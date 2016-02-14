package in.reeltime.deployment.configuration

import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting
import in.reeltime.deployment.aws.client.EnhancedAmazonEC2
import in.reeltime.deployment.aws.client.EnhancedAmazonIdentityManagement

import static in.reeltime.deployment.log.StatusLogger.*

class AwsBeanstalkEnvironmentConfiguration {

    // Launch options
    private static final String LAUNCH_CONFIGURATION_NAMESPACE = 'aws:autoscaling:launchconfiguration'
    private static final String IAM_INSTANCE_PROFILE = 'IamInstanceProfile'
    private static final String INSTANCE_TYPE = 'InstanceType'
    private static final String SECURITY_GROUPS = 'SecurityGroups'

    // VPC options
    private static final String VPC_NAMESPACE = 'aws:ec2:vpc'
    private static final String VPC_ID = 'VPCId'
    private static final String SUBNETS = 'Subnets'
    private static final String ELB_SUBNETS = 'ELBSubnets'
    private static final String ASSOCIATE_PUBLIC_IP_ADDRESS = 'AssociatePublicIpAddress'

    // Environment options
    private static final String ENVIRONMENT_NAMESPACE = 'aws:elasticbeanstalk:environment'
    private static final String ENVIRONMENT_TYPE = 'EnvironmentType'

    // Application status options
    private static final String APPLICATION_NAMESPACE = 'aws:elasticbeanstalk:application'
    private static final String HEALTHCHECK_URL = 'Application Healthcheck URL'

    // Load balancer options
    private static final String LOAD_BALANCER_NAMESPACE = 'aws:elb:loadbalancer'
    private static final String LOAD_BALANCER_HTTP_PORT = 'LoadBalancerHTTPPort'
    private static final String LOAD_BALANCER_HTTPS_PORT = 'LoadBalancerHTTPSPort'
    private static final String LOAD_BALANCER_SSL_CERTIFICATE_ID = 'SSLCertificateId'

    // JVM options
    private static final String JVM_NAMESPACE = 'aws:elasticbeanstalk:container:tomcat:jvmoptions'
    private static final String JVM_MAX_HEAP_SIZE = 'Xmx'
    private static final String JVM_MAX_PERM_SIZE = 'XX:MaxPermSize'
    private static final String JVM_INIT_HEAP_SIZE = 'Xms'

    private DeploymentConfiguration deployConfig

    private EnhancedAmazonEC2 ec2
    private EnhancedAmazonIdentityManagement iam

    AwsBeanstalkEnvironmentConfiguration(DeploymentConfiguration deployConfig, EnhancedAmazonEC2 ec2,
                                         EnhancedAmazonIdentityManagement iam) {
        this.deployConfig = deployConfig
        this.ec2 = ec2
        this.iam = iam
    }

    Collection<ConfigurationOptionSetting> getConfigurationOptionSettings() {

        String instanceProfileName = deployConfig.configuration.launch.instanceProfileName
        String instanceType = deployConfig.configuration.launch.instanceType

        String environmentType = deployConfig.configuration.environment.type
        String healthCheckUrl = deployConfig.configuration.application.healthCheckUrl

        String jvmMaxHeapSize = deployConfig.configuration.jvm.maxHeapSize
        String jvmMaxPermSize = deployConfig.configuration.jvm.maxPermSize
        String jvmInitHeapSize = deployConfig.configuration.jvm.initHeapSize

        Collection<ConfigurationOptionSetting> configurationOptions = [

                new ConfigurationOptionSetting(LAUNCH_CONFIGURATION_NAMESPACE, IAM_INSTANCE_PROFILE, instanceProfileName),
                new ConfigurationOptionSetting(LAUNCH_CONFIGURATION_NAMESPACE, INSTANCE_TYPE, instanceType),

                new ConfigurationOptionSetting(ENVIRONMENT_NAMESPACE, ENVIRONMENT_TYPE, environmentType),

                new ConfigurationOptionSetting(APPLICATION_NAMESPACE, HEALTHCHECK_URL, healthCheckUrl),

                new ConfigurationOptionSetting(JVM_NAMESPACE, JVM_MAX_HEAP_SIZE, jvmMaxHeapSize),
                new ConfigurationOptionSetting(JVM_NAMESPACE, JVM_MAX_PERM_SIZE, jvmMaxPermSize),
                new ConfigurationOptionSetting(JVM_NAMESPACE, JVM_INIT_HEAP_SIZE, jvmInitHeapSize)
        ]

        displayStatus("Adding security groups configuration options")
        configurationOptions += collectSecurityGroups()

        if(deployConfig.targetEnvironmentIsLoadBalanced()) {
            displayStatus("Adding load balancer configuration options")
            configurationOptions += collectLoadBalancerOptions()
        }

        if(deployConfig.targetEnvironmentIsInVpc()) {
            displayStatus("Adding VPC configuration options")
            configurationOptions += collectVpcConfigurationOptionSettings(configurationOptions)
        }
        return configurationOptions
    }

    // Only one security group can be specified for the initial launch
    // Additional security groups must be added to the instance post-launch
    private Collection<ConfigurationOptionSetting> collectSecurityGroups() {

        Collection<ConfigurationOptionSetting> securityGroups = []
        String securityGroup = deployConfig.configuration.launch.securityGroup

        if(securityGroup) {
            // Security Group ID must be used for a VPC environment
            if(deployConfig.targetEnvironmentIsInVpc()) {
                securityGroup = ec2.findSecurityGroupByGroupName(securityGroup).groupId
            }

            securityGroups << new ConfigurationOptionSetting(LAUNCH_CONFIGURATION_NAMESPACE, SECURITY_GROUPS, securityGroup)
        }

        return securityGroups
    }

    private Collection<ConfigurationOptionSetting> collectLoadBalancerOptions() {

        String certificateName = deployConfig.configuration.loadBalancer.certificateName
        String certificateArn = iam.findServerCertificateArnByName(certificateName)

        [
                new ConfigurationOptionSetting(LOAD_BALANCER_NAMESPACE, LOAD_BALANCER_HTTP_PORT, 'OFF'),
                new ConfigurationOptionSetting(LOAD_BALANCER_NAMESPACE, LOAD_BALANCER_HTTPS_PORT, '443'),
                new ConfigurationOptionSetting(LOAD_BALANCER_NAMESPACE, LOAD_BALANCER_SSL_CERTIFICATE_ID, certificateArn)
        ]
    }

    private Collection<ConfigurationOptionSetting> collectVpcConfigurationOptionSettings(Collection<ConfigurationOptionSetting> configurationOptions) {

        String vpcId = deployConfig.configuration.vpc.vpcId

        String autoScalingSubnetName = deployConfig.configuration.vpc.autoScalingSubnetName
        String autoScalingSubnetId = ec2.findSubnetByVpcIdAndSubnetName(vpcId, autoScalingSubnetName).subnetId

        String loadBalancerSubnetName = deployConfig.configuration.vpc.loadBalancerSubnetName
        String loadBalancerSubnetId = ec2.findSubnetByVpcIdAndSubnetName(vpcId, loadBalancerSubnetName).subnetId

        [
                new ConfigurationOptionSetting(VPC_NAMESPACE, VPC_ID, vpcId),
                new ConfigurationOptionSetting(VPC_NAMESPACE, SUBNETS, autoScalingSubnetId),
                new ConfigurationOptionSetting(VPC_NAMESPACE, ELB_SUBNETS, loadBalancerSubnetId),
                new ConfigurationOptionSetting(VPC_NAMESPACE, ASSOCIATE_PUBLIC_IP_ADDRESS, 'false')
        ]
    }

}
