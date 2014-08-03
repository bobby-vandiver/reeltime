import com.amazonaws.services.ec2.model.IpPermission
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest
import com.amazonaws.services.ec2.model.SecurityGroup
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier
import com.amazonaws.services.elasticbeanstalk.model.S3Location
import com.amazonaws.services.elasticbeanstalk.model.SourceBundleDeletionException
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest
import com.amazonaws.services.sns.model.SubscribeRequest

import java.text.SimpleDateFormat

includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsWar")

includeTargets << new File("${basedir}/scripts/_DeployConfig.groovy")
includeTargets << new File("${basedir}/scripts/_Common.groovy")
includeTargets << new File("${basedir}/scripts/_AwsClients.groovy")
includeTargets << new File("${basedir}/scripts/_AwsBeanstalkEnvironmentConfiguration.groovy")

target(deployWar: "Builds and deploys the WAR") {
    depends(loadDeployConfig, initAwsClients, compile, classpath, war)

    ConfigObject applicationProperties = loadApplicationProperties()

    String projectName = applicationProperties.app.name
    String projectVersion = applicationProperties.app.version

    String applicationName = 'ReelTime'
    String applicationVersion = projectVersion + '-' + grailsEnvironmentName()

    displayStatus("Deploying application [$applicationName] version [$applicationVersion]...")

    if(!eb.applicationExists(applicationName)) {
        displayStatus("Application [$applicationName] does not exist -- creating...")
        eb.createApplication(applicationName)
    }

    boolean production = targetEnvironmentIsProduction()
    boolean applicationVersionExists = eb.applicationVersionExists(applicationName, applicationVersion)

    File war = getWar(projectName, projectVersion)

    String bucket = deployConfig.storage.warBucket
    String key = war.name

    // Prefix instead of append build time for production to avoid file extension problems
    if(production) {
        String timestamp = generateBuildTimestamp()
        applicationVersion = "$timestamp-$applicationVersion"
        key = "$timestamp-$key"
    }

    String environmentName = deployConfig.environment.name

    // The production environment should never be terminated
    if(!production && applicationVersionExists) {
        terminateEnvironment(applicationName, environmentName, applicationVersion)
        deleteExistingSnapshotApplicationVersion(applicationName, applicationVersion, bucket, key)
    }

    // Either the previous acceptance build will have been removed or the production WAR will have unique timestamp
    if(s3.objectExists(bucket, key)) {
        displayStatus("Object [$key] in bucket [$bucket] already exists")
        System.exit(1)
    }

    createNewApplicationVersion(applicationName, bucket, key, applicationVersion, war)

    // Production environment(s) should be the only environment that is ever updated
    // The acceptance environment will be torn down and rebuilt each time to ensure a clean slate
    if(production && eb.environmentExists(applicationName, environmentName)) {
        displayStatus("Updating environment [$environmentName] to version [$applicationVersion]")
        updateEnvironment(environmentName, applicationVersion)
    }
    else {
        displayStatus("Creating environment [$environmentName] for version [$applicationVersion]")
        createEnvironment(applicationName, environmentName, applicationVersion)
    }
    waitUntilEnvironmentIsReady(applicationName, environmentName, applicationVersion)

    EnvironmentDescription environment = eb.findEnvironment(applicationName, environmentName)

    // AWS will set up the EC2 instances so they only accept traffic on port 80 from the load balancer
    // this is required for the load balancer to perform health checks on the running instances
    if(!targetEnvironmentIsLoadBalanced()) {
        disableHttpAccess(environment)
    }

    subscribeToTranscoderTopic(environment)

    displayStatus("Successfully deployed WAR.")
    displayStatus("Endpoint URL: ${environment.endpointURL}")
    displayStatus("CNAME: ${environment.CNAME}")
}

String generateBuildTimestamp() {
    def dateFormat = new SimpleDateFormat('MM-dd-yyyy-HH:mm')
    def now = new Date()
    return dateFormat.format(now)
}

void waitUntilEnvironmentIsReady(String applicationName, String environmentName, String version) {
    String statusMessage = "Waiting for environment to go live."
    String failureMessage = "Exceeded max retries for polling environment status. Check AWS console for more info."
    long pollingInterval = 30 * 1000

    waitForCondition(statusMessage, failureMessage, pollingInterval) {
        return eb.environmentIsReady(applicationName, environmentName, version)
    }
}

void subscribeToTranscoderTopic(EnvironmentDescription environment) {
    String endpoint = 'https://' + environment.CNAME + '/transcoder/notification'

    ['completed', 'progressing', 'warning', 'error'].each { action ->
        SubscribeRequest request = new SubscribeRequest(transcoderTopicArn, 'https', "${endpoint}/${action}")

        displayStatus("Subscribing endpoint [$endpoint] to topic [$transcoderTopicArn]: $request")
        sns.subscribe(request)
    }
}

void disableHttpAccess(EnvironmentDescription environment) {
    String environmentId = environment.environmentId
    Collection<SecurityGroup> securityGroups = ec2.findSecurityGroupsByEnvironmentId(environmentId)

    securityGroups.each { securityGroup ->

        IpPermission httpAccess = securityGroup?.ipPermissions?.find { rule ->
            rule.toPort == 80 && rule.fromPort == 80 && rule.ipProtocol == 'tcp'
        }

        if(httpAccess) {
            String groupId = securityGroup.groupId
            RevokeSecurityGroupIngressRequest request = new RevokeSecurityGroupIngressRequest(groupId: groupId, ipPermissions: [httpAccess])

            displayStatus("Disabling HTTP access: ${request}")
            ec2.revokeSecurityGroupIngress(request)
        }
    }
}

void terminateEnvironment(String applicationName, String environmentName, String version) {

    if(!eb.environmentExistsForVersion(applicationName, environmentName, version)) {
        displayStatus("Environment [$environmentName] does not exist for version [$version]")
        return
    }

    TerminateEnvironmentRequest request = new TerminateEnvironmentRequest(environmentName: environmentName)

    displayStatus("Terminating environment [$environmentName]")
    eb.terminateEnvironment(request)

    String statusMessage = "Waiting for environment to terminate."
    String failureMessage = "Exceeded max retries for polling environment health. Check AWS console for more info."
    long pollingInterval = 20 * 1000

    waitForCondition(statusMessage, failureMessage, pollingInterval) {
        return !eb.environmentExistsForVersion(applicationName, environmentName, version)
    }

    displayStatus("Successfully terminated environment.")
}

void deleteExistingSnapshotApplicationVersion(String applicationName, String version, String bucket, String key) {
    try {
        DeleteApplicationVersionRequest request = new DeleteApplicationVersionRequest(
                applicationName: applicationName,
                versionLabel: version,
                deleteSourceBundle: true
        )
        displayStatus("Deleting version [$version] and the associated WAR in S3")
        eb.deleteApplicationVersion(request)
    }
    catch(SourceBundleDeletionException e) {
        displayStatus("Failed to delete source bundle automatically")
        displayStatus("AWS exception message: ${e.message}")

        displayStatus("Attempting to delete object [$bucket :: $key] from S3 directly")
        s3.deleteObject(bucket, key)
    }
}

void createEnvironment(String applicationName, String environmentName, String version) {
    String solutionStack = deployConfig.environment.solutionStackName

    // The application server will always be a web server
    EnvironmentTier tier = new EnvironmentTier(name: 'WebServer', type: 'Standard', version: '1.0')

    CreateEnvironmentRequest createEnvironmentRequest = new CreateEnvironmentRequest(
            applicationName: applicationName,
            versionLabel: version,
            environmentName: environmentName,
            tier: tier,
            solutionStackName: solutionStack,
            optionSettings: getConfigurationOptionSettings()
    )

    displayStatus("Creating environment: $createEnvironmentRequest")
    eb.createEnvironment(createEnvironmentRequest)
}

void updateEnvironment(String environmentName, String version) {

    UpdateEnvironmentRequest updateEnvironmentRequest = new UpdateEnvironmentRequest(
            environmentName: environmentName,
            versionLabel: version
    )

    displayStatus("Updating environment: $updateEnvironmentRequest")
    eb.updateEnvironment(updateEnvironmentRequest)
}

void createNewApplicationVersion(String applicationName, String bucket, String key, String version, File war) {
    displayStatus("Uploading war [${war.name}] to S3 bucket [$bucket] with key [$key]")
    s3.uploadFile(war, bucket, key)

    CreateApplicationVersionRequest createApplicationVersionRequest = new CreateApplicationVersionRequest(
            applicationName: applicationName,
            versionLabel: version,
            sourceBundle: new S3Location(bucket, key)
    )

    displayStatus("Creating appliction version: $version")
    eb.createApplicationVersion(createApplicationVersionRequest)
}

File getWar(String name, String version) {
    File war = new File("target/${name}-${version}.war")
    if(!war.exists()) {
        displayStatus("WAR file does not exist -- can't deploy!")
        System.exit(1)
    }
    return war
}