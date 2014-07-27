import com.amazonaws.services.ec2.model.IpPermission
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest
import com.amazonaws.services.ec2.model.SecurityGroup
import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting
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

includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsWar")

includeTargets << new File("${basedir}/scripts/_DeployConfig.groovy")
includeTargets << new File("${basedir}/scripts/_Common.groovy")
includeTargets << new File("${basedir}/scripts/_AwsClients.groovy")

target(deployWar: "Builds and deploys the WAR") {
    depends(loadDeployConfig, initAwsClients, compile, classpath, war)

    ConfigObject applicationProperties = loadApplicationProperties()

    String name = applicationProperties.app.name
    String version = applicationProperties.app.version

// TODO: Change this for the final version to use app name
    String applicationName = 'deployment-test'

    if(!eb.applicationExists(applicationName)) {
        displayStatus("Application [$applicationName] does not exist -- creating...")
        eb.createApplication(applicationName)
    }

    boolean snapshot = version.endsWith('SNAPSHOT')
    boolean versionExists = eb.applicationVersionExists(applicationName, version)

    File war = getWar(name, version)

    String bucket = deployConfig.storage.warBucket
    String key = war.name

    String environmentName = deployConfig.environment.name

    if(versionExists && snapshot) {
        terminateEnvironment(applicationName, environmentName, version)
        deleteExistingSnapshotApplicationVersion(applicationName, version, bucket, key)
    }
    else if(versionExists) {
        displayStatus("Deploying versioned WAR is not allowed")
        System.exit(1)
    }

    if(s3.objectExists(bucket, key)) {
        displayStatus("Object [$key] in bucket [$bucket] already exists")
        System.exit(1)
    }

    createNewApplicationVersion(applicationName, bucket, key, version, war)

    // Production environment(s) should be the only environment that is ever updated
    // The staging environment will be torn down and rebuilt each time to ensure a clean slate
    if(!snapshot && eb.environmentExists(applicationName, environmentName)) {
        displayStatus("Updating environment [$environmentName] to version [$version]")
        updateEnvironment(environmentName, version)
    }
    else {
        displayStatus("Creating environment [$environmentName] for version [$version]")
        createEnvironment(applicationName, environmentName, version)
    }
    waitUntilEnvironmentIsReady(applicationName, environmentName, version)

    EnvironmentDescription environment = eb.findEnvironment(applicationName, environmentName)
    disableHttpAccess(environment)
    subscribeToTranscoderTopic(environment)

    displayStatus("Successfully deployed WAR.")
    displayStatus("Endpoint URL: ${environment.endpointURL}")
    displayStatus("CNAME: ${environment.CNAME}")
}

void waitUntilEnvironmentIsReady(String applicationName, String environmentName, String version) {
    String statusMessage = "Waiting for environment to go live."
    String failureMessage = "Exceeded max retries for polling environment status. Check AWS console for more info."
    long pollingInterval = 20 * 1000

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
    SecurityGroup securityGroup = ec2.findSecurityGroupByEnvironmentId(environmentId)

    IpPermission httpAccess = securityGroup?.ipPermissions?.find { rule ->
        rule.toPort == 80 && rule.fromPort == 80 && rule.ipProtocol == 'tcp'
    }

    String groupName = securityGroup.groupName
    RevokeSecurityGroupIngressRequest request = new RevokeSecurityGroupIngressRequest(groupName, [httpAccess])

    displayStatus("Disabling HTTP access")
    ec2.revokeSecurityGroupIngress(request)
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
            optionSettings: configurationOptionSettings
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

Collection<ConfigurationOptionSetting> getConfigurationOptionSettings() {

    // Launch options
    final String LAUNCH_CONFIGURATION_NAMESPACE = 'aws:autoscaling:launchconfiguration'
    final String IAM_INSTANCE_PROFILE = 'IamInstanceProfile'
    final String SECURITY_GROUPS = 'SecurityGroups'

    // Environment options
    final String ENVIRONMENT_NAMESPACE = 'aws:elasticbeanstalk:environment'
    final String ENVIRONMENT_TYPE = 'EnvironmentType'

    // Application status options
    final String APPLICATION_NAMESPACE = 'aws:elasticbeanstalk:application'
    final String HEALTHCHECK_URL = 'Application Healthcheck URL'

    // JVM options
    final String JVM_NAMESPACE = 'aws:elasticbeanstalk:container:tomcat:jvmoptions'
    final String JVM_MAX_HEAP_SIZE = 'Xmx'
    final String JVM_MAX_PERM_SIZE = 'XX:MaxPermSize'
    final String JVM_INIT_HEAP_SIZE = 'Xms'

    String instanceProfileName = deployConfig.launch.instanceProfileName

    String securityGroupName = deployConfig.launch.securityGroupName
    String securityGroupId = deployConfig.launch.securityGroupId

    // Security Group ID must be used for a VPC environment
    String securityGroup = targetEnvironmentIsLoadBalanced() ? securityGroupId : securityGroupName

    String environmentType = deployConfig.environment.type
    String healthCheckUrl = deployConfig.application.healthCheckUrl

    String jvmMaxHeapSize = deployConfig.jvm.maxHeapSize
    String jvmMaxPermSize = deployConfig.jvm.maxPermSize
    String jvmInitHeapSize = deployConfig.jvm.initHeapSize

    Collection<ConfigurationOptionSetting> configurationOptions =
    [
            new ConfigurationOptionSetting(LAUNCH_CONFIGURATION_NAMESPACE, IAM_INSTANCE_PROFILE, instanceProfileName),
            new ConfigurationOptionSetting(LAUNCH_CONFIGURATION_NAMESPACE, SECURITY_GROUPS, securityGroup),

            new ConfigurationOptionSetting(ENVIRONMENT_NAMESPACE, ENVIRONMENT_TYPE, environmentType),

            new ConfigurationOptionSetting(APPLICATION_NAMESPACE, HEALTHCHECK_URL, healthCheckUrl),

            new ConfigurationOptionSetting(JVM_NAMESPACE, JVM_MAX_HEAP_SIZE, jvmMaxHeapSize),
            new ConfigurationOptionSetting(JVM_NAMESPACE, JVM_MAX_PERM_SIZE, jvmMaxPermSize),
            new ConfigurationOptionSetting(JVM_NAMESPACE, JVM_INIT_HEAP_SIZE, jvmInitHeapSize)
    ]

    if(targetEnvironmentIsLoadBalanced()) {
        displayStatus("Adding load balanced environment configuration options")
        configurationOptions = addLoadBalancedConfigurationOptionSettings(configurationOptions)
    }
    return configurationOptions
}

Collection<ConfigurationOptionSetting> addLoadBalancedConfigurationOptionSettings(Collection<ConfigurationOptionSetting> configurationOptions) {

    // VPC options
    final String VPC_NAMESPACE = 'aws:ec2:vpc'
    final String VPC_ID = 'VPCId'
    final String SUBNETS = 'Subnets'
    final String ELB_SUBNETS = 'ELBSubnets'

    String vpcId = deployConfig.vpc.vpcId
    String autoScalingSubnetId = deployConfig.vpc.autoScalingSubnetId
    String loadBalancerSubnetId = deployConfig.vpc.loadBalancerSubnetId

    return configurationOptions + [
            new ConfigurationOptionSetting(VPC_NAMESPACE, VPC_ID, vpcId),
            new ConfigurationOptionSetting(VPC_NAMESPACE, SUBNETS, autoScalingSubnetId),
            new ConfigurationOptionSetting(VPC_NAMESPACE, ELB_SUBNETS, loadBalancerSubnetId)
    ]
}


File getWar(String name, String version) {
    File war = new File("target/${name}-${version}.war")
    if(!war.exists()) {
        displayStatus("WAR file does not exist -- can't deploy!")
        System.exit(1)
    }
    return war
}