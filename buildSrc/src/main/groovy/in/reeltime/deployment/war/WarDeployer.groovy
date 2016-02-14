package in.reeltime.deployment.war

import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.IpPermission
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest
import com.amazonaws.services.ec2.model.SecurityGroup
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest
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
import in.reeltime.deployment.aws.client.EnhancedAWSElasticBeanstalk
import in.reeltime.deployment.aws.client.EnhancedAmazonEC2
import in.reeltime.deployment.aws.client.EnhancedAmazonS3
import in.reeltime.deployment.aws.client.EnhancedAmazonSNS
import in.reeltime.deployment.configuration.ApplicationConfiguration
import in.reeltime.deployment.configuration.AwsBeanstalkEnvironmentConfiguration
import in.reeltime.deployment.configuration.DeploymentConfiguration
import in.reeltime.deployment.transcoder.TranscoderProvisioner
import org.gradle.api.Project

import java.text.SimpleDateFormat

import static in.reeltime.deployment.log.StatusLogger.*
import static in.reeltime.deployment.condition.ConditionalWait.*

class WarDeployer {

    @Delegate
    private Project project

    private DeploymentConfiguration deployConfig
    private AwsBeanstalkEnvironmentConfiguration beanstalkConfig

    private TranscoderProvisioner transcoderProvisioner

    private EnhancedAmazonS3 s3
    private EnhancedAmazonEC2 ec2
    private EnhancedAmazonSNS sns
    private EnhancedAWSElasticBeanstalk eb

    WarDeployer(Project project, DeploymentConfiguration deployConfig, AwsBeanstalkEnvironmentConfiguration beanstalkConfig,
                TranscoderProvisioner transcoderProvisioner, EnhancedAmazonEC2 ec2, EnhancedAmazonSNS sns,
                EnhancedAmazonS3 s3, EnhancedAWSElasticBeanstalk eb) {

        this.project = project

        this.deployConfig = deployConfig
        this.beanstalkConfig = beanstalkConfig

        this.transcoderProvisioner = transcoderProvisioner

        this.s3 = s3
        this.ec2 = ec2
        this.sns = sns
        this.eb = eb
    }

    void deployWar() {

        String projectName = project.name
        String projectVersion = project.version

        String applicationName = ApplicationConfiguration.APPLICATION_NAME
        String applicationVersion = projectVersion + '-' + deployConfig.environment

        displayStatus("Deploying application [$applicationName] version [$applicationVersion]...")

        if(!eb.applicationExists(applicationName)) {
            displayStatus("Application [$applicationName] does not exist -- creating...")
            createApplication(applicationName)
        }

        boolean production = deployConfig.targetEnvironmentIsProduction()
        boolean applicationVersionExists = eb.applicationVersionExists(applicationName, applicationVersion)

        File war = getWar(projectName, projectVersion)

        String bucket = deployConfig.configuration.storage.warBucket
        String key = war.name

        // Prefix instead of append build time for production to avoid file extension problems
        if(production) {
            String timestamp = generateBuildTimestamp()
            applicationVersion = "$timestamp-$applicationVersion"
            key = "$timestamp-$key"
        }

        String environmentName = deployConfig.configuration.environment.name

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
        if(!deployConfig.targetEnvironmentIsLoadBalanced()) {
            disableHttpAccessFromAnywhere(environment)

            // The endpointUrl will be the public IP address for single instances
            Instance instance = ec2.findInstanceByPublicIpAddress(environment.endpointURL)
            addPostLaunchSecurityGroups(instance)
        }

        subscribeToTranscoderTopic(environment)

        displayStatus("Successfully deployed WAR.")
        displayStatus("Endpoint URL: ${environment.endpointURL}")
        displayStatus("CNAME: ${environment.CNAME}")
    }

    private void createApplication(String applicationName) {
        def request = new CreateApplicationRequest(applicationName)
        def result = eb.createApplication(request)
        displayStatus("Create application result: $result")
    }

    private String generateBuildTimestamp() {
        def dateFormat = new SimpleDateFormat('MM-dd-yyyy-HH:mm')
        def now = new Date()
        return dateFormat.format(now)
    }

    private void waitUntilEnvironmentIsReady(String applicationName, String environmentName, String version) {
        String statusMessage = "Waiting for environment to go live."
        String failureMessage = "Exceeded max retries for polling environment status. Check AWS console for more info."
        long pollingInterval = 30 * 1000

        waitForCondition(statusMessage, failureMessage, pollingInterval) {
            return eb.environmentIsReady(applicationName, environmentName, version)
        }
    }

    private void subscribeToTranscoderTopic(EnvironmentDescription environment) {
        String protocol = 'https'

        // SNS will only publish notifications over HTTPS if the app has a valid certificate
        // and will not allow self-signed certs, so we allow HTTP for the acceptance environment.
        if(!deployConfig.targetEnvironmentIsLoadBalanced() && !deployConfig.targetEnvironmentIsProduction()) {
            protocol = 'http'
        }

        String transcoderTopicArn = transcoderProvisioner.transcoderTopicArn

        String endpoint = protocol + '://' + environment.CNAME + '/aws/transcoder/notification'
        SubscribeRequest request = new SubscribeRequest(transcoderTopicArn, protocol, endpoint)

        displayStatus("Subscribing endpoint [$endpoint] to topic [$transcoderTopicArn]: $request")
        sns.subscribe(request)
    }

    private void disableHttpAccessFromAnywhere(EnvironmentDescription environment) {
        String environmentId = environment.environmentId
        Collection<SecurityGroup> securityGroups = ec2.findSecurityGroupsByEnvironmentId(environmentId)

        securityGroups.each { securityGroup ->

            IpPermission httpAccess = securityGroup?.ipPermissions?.find { rule ->
                rule.toPort == 80 && rule.fromPort == 80 && rule.ipProtocol == 'tcp' && rule.ipRanges == ['0.0.0.0/0']
            }

            if(httpAccess) {
                String groupId = securityGroup.groupId
                RevokeSecurityGroupIngressRequest request = new RevokeSecurityGroupIngressRequest(groupId: groupId, ipPermissions: [httpAccess])

                displayStatus("Disabling HTTP access: ${request}")
                ec2.revokeSecurityGroupIngress(request)
            }
        }
    }

    private void addPostLaunchSecurityGroups(Instance instance) {
        if(!instance) {
            displayStatus("Invalid instance specified!")
            System.exit(1)
        }

        Collection<String> securityGroupIds = instance.securityGroups.collect { it.groupId }
        Collection<String> securityGroupsToAdd = deployConfig.configuration.postLaunch.securityGroups

        if(securityGroupsToAdd.empty) {
            displayStatus("No additional security groups to add to instance ${instance.instanceId}")
            return
        }

        securityGroupsToAdd.each { groupName ->
            def securityGroup = ec2.findSecurityGroupByGroupName(groupName)

            if(securityGroup) {
                def groupId = securityGroup.groupId

                if(!securityGroupIds.contains(groupId)) {
                    displayStatus("Adding [$groupName] to list of security groups to apply")
                    securityGroupIds << groupId
                }
                else {
                    displayStatus("Instance already contains security group [$groupName]")
                }
            }
            else {
                displayStatus("Could not find security group named [$groupName]")
            }
        }

        ModifyInstanceAttributeRequest request = new ModifyInstanceAttributeRequest(instanceId: instance.instanceId, groups: securityGroupIds)

        displayStatus("Modifying instance ${instance.instanceId}: ${request}")
        ec2.modifyInstanceAttribute(request)
    }

    private void terminateEnvironment(String applicationName, String environmentName, String version) {

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

    private void deleteExistingSnapshotApplicationVersion(String applicationName, String version, String bucket, String key) {
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

    private void createEnvironment(String applicationName, String environmentName, String version) {
        String solutionStack = deployConfig.configuration.environment.solutionStackName

        // The application server will always be a web server
        EnvironmentTier tier = new EnvironmentTier(name: 'WebServer', type: 'Standard', version: '1.0')

        CreateEnvironmentRequest createEnvironmentRequest = new CreateEnvironmentRequest(
                applicationName: applicationName,
                versionLabel: version,
                environmentName: environmentName,
                tier: tier,
                solutionStackName: solutionStack,
                optionSettings: beanstalkConfig.configurationOptionSettings
        )

        displayStatus("Creating environment: $createEnvironmentRequest")
        eb.createEnvironment(createEnvironmentRequest)
    }

    private void updateEnvironment(String environmentName, String version) {

        UpdateEnvironmentRequest updateEnvironmentRequest = new UpdateEnvironmentRequest(
                environmentName: environmentName,
                versionLabel: version
        )

        displayStatus("Updating environment: $updateEnvironmentRequest")
        eb.updateEnvironment(updateEnvironmentRequest)
    }

    private void createNewApplicationVersion(String applicationName, String bucket, String key, String version, File war) {
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

    private File getWar(String name, String version) {
        File war = new File("${buildDir}/libs/${name}-${version}.war")
        if(!war.exists()) {
            displayStatus("WAR file does not exist -- can't deploy!")
            System.exit(1)
        }
        return war
    }
}
