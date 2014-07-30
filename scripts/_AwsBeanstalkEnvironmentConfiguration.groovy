import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting

includeTargets << new File("${basedir}/scripts/_DeployConfig.groovy")
includeTargets << new File("${basedir}/scripts/_Common.groovy")

// Launch options
LAUNCH_CONFIGURATION_NAMESPACE = 'aws:autoscaling:launchconfiguration'
IAM_INSTANCE_PROFILE = 'IamInstanceProfile'
SECURITY_GROUPS = 'SecurityGroups'

// VPC options
VPC_NAMESPACE = 'aws:ec2:vpc'
VPC_ID = 'VPCId'
SUBNETS = 'Subnets'
ELB_SUBNETS = 'ELBSubnets'

// Environment options
ENVIRONMENT_NAMESPACE = 'aws:elasticbeanstalk:environment'
ENVIRONMENT_TYPE = 'EnvironmentType'

// Application status options
APPLICATION_NAMESPACE = 'aws:elasticbeanstalk:application'
HEALTHCHECK_URL = 'Application Healthcheck URL'

// Load balancer options
LOAD_BALANCER_NAMESPACE = 'aws:elb:loadbalancer'
LOAD_BALANCER_HTTP_PORT = 'LoadBalancerHTTPPort'
LOAD_BALANCER_HTTPS_PORT = 'LoadBalancerHTTPSPort'
LOAD_BALANCER_SSL_CERTIFICATE_ID = 'SSLCertificateId'

// JVM options
JVM_NAMESPACE = 'aws:elasticbeanstalk:container:tomcat:jvmoptions'
JVM_MAX_HEAP_SIZE = 'Xmx'
JVM_MAX_PERM_SIZE = 'XX:MaxPermSize'
JVM_INIT_HEAP_SIZE = 'Xms'

getConfigurationOptionSettings = {

    String instanceProfileName = deployConfig.launch.instanceProfileName

    String environmentType = deployConfig.environment.type
    String healthCheckUrl = deployConfig.application.healthCheckUrl

    String jvmMaxHeapSize = deployConfig.jvm.maxHeapSize
    String jvmMaxPermSize = deployConfig.jvm.maxPermSize
    String jvmInitHeapSize = deployConfig.jvm.initHeapSize

    Collection<ConfigurationOptionSetting> configurationOptions = [

            new ConfigurationOptionSetting(LAUNCH_CONFIGURATION_NAMESPACE, IAM_INSTANCE_PROFILE, instanceProfileName),

            new ConfigurationOptionSetting(ENVIRONMENT_NAMESPACE, ENVIRONMENT_TYPE, environmentType),

            new ConfigurationOptionSetting(APPLICATION_NAMESPACE, HEALTHCHECK_URL, healthCheckUrl),

            new ConfigurationOptionSetting(JVM_NAMESPACE, JVM_MAX_HEAP_SIZE, jvmMaxHeapSize),
            new ConfigurationOptionSetting(JVM_NAMESPACE, JVM_MAX_PERM_SIZE, jvmMaxPermSize),
            new ConfigurationOptionSetting(JVM_NAMESPACE, JVM_INIT_HEAP_SIZE, jvmInitHeapSize)
    ]

    displayStatus("Adding security groups configuration options")
    configurationOptions += collectSecurityGroups()

    if(targetEnvironmentIsLoadBalanced()) {
        displayStatus("Adding load balancer configuration options")
        configurationOptions += collectLoadBalancerOptions()
    }

    if(targetEnvironmentIsInVpc()) {
        displayStatus("Adding VPC configuration options")
        configurationOptions += collectVpcConfigurationOptionSettings(configurationOptions)
    }
    return configurationOptions
}

private Collection<ConfigurationOptionSetting> collectSecurityGroups() {

    Collection<ConfigurationOptionSetting> securityGroups = []
    List<String> securityGroupNames = deployConfig.launch.securityGroupNames

    securityGroupNames.each { groupName ->
        String securityGroup = groupName

        // Security Group ID must be used for a VPC environment
        if(targetEnvironmentIsInVpc()) {
            securityGroup = ec2.findSecurityGroupIdByGroupName(groupName).groupId
        }

        securityGroups << new ConfigurationOptionSetting(LAUNCH_CONFIGURATION_NAMESPACE, SECURITY_GROUPS, securityGroup)
    }
    return securityGroups
}

private Collection<ConfigurationOptionSetting> collectLoadBalancerOptions() {

    String certificateName = deployConfig.loadBalancer.certificateName
    String certificateArn = iam.findServerCertificateArnByName(certificateName)

    [
            new ConfigurationOptionSetting(LOAD_BALANCER_NAMESPACE, LOAD_BALANCER_HTTP_PORT, 'OFF'),
            new ConfigurationOptionSetting(LOAD_BALANCER_NAMESPACE, LOAD_BALANCER_HTTPS_PORT, '443'),
            new ConfigurationOptionSetting(LOAD_BALANCER_NAMESPACE, LOAD_BALANCER_SSL_CERTIFICATE_ID, certificateArn)
    ]
}

private Collection<ConfigurationOptionSetting> collectVpcConfigurationOptionSettings(Collection<ConfigurationOptionSetting> configurationOptions) {

    String vpcId = deployConfig.vpc.vpcId

    String autoScalingSubnetName = deployConfig.vpc.autoScalingSubnetName
    String autoScalingSubnetId = ec2.findSubnetByVpcIdAndSubnetName(vpcId, autoScalingSubnetName).subnetId

    String loadBalancerSubnetName = deployConfig.vpc.loadBalancerSubnetName
    String loadBalancerSubnetId = ec2.findSubnetByVpcIdAndSubnetName(vpcId, loadBalancerSubnetName).subnetId

    [
            new ConfigurationOptionSetting(VPC_NAMESPACE, VPC_ID, vpcId),
            new ConfigurationOptionSetting(VPC_NAMESPACE, SUBNETS, autoScalingSubnetId),
            new ConfigurationOptionSetting(VPC_NAMESPACE, ELB_SUBNETS, loadBalancerSubnetId)
    ]
}
