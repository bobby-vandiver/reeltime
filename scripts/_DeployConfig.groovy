import grails.util.Environment

includeTargets << new File("${basedir}/scripts/_Common.groovy")

target(loadDeployConfig: "Populates the deployConfig map based on the current Grails environment") {

    def currentEnvironment = Environment.currentEnvironment.name
    displayStatus("Loading deployment configuration for environment [$currentEnvironment]")

    if(currentEnvironment == 'production') {
        deployConfig = loadProductionConfig()
    }
    else if (currentEnvironment == 'acceptance') {
        deployConfig = loadSingleInstanceConfig()
    }
    else {
        displayStatus("Deployment from the current environment [$currentEnvironment] is not supported")
        System.exit(1)
    }
}

resetResourcesIsAllowed = {
    return deployConfig.resetResources == true
}

targetEnvironmentIsInVpc = {
    return deployConfig.vpc != null
}

targetEnvironmentIsProduction = {
    return grailsEnvironmentName() == 'production'
}

// TODO: Refactor common options that are not environment specific, e.g. solution stack
// TODO: Production config should be load balanced
Map loadProductionConfig() {
    [
            resetResources: false,

            launch: [
                    instanceProfileName: 'EC2-Instance-Test-Role',
                    securityGroupNames: ['NAT-SG', 'SSL-SG']
            ],

            vpc: [
                    vpcId: 'vpc-20f35345',
                    loadBalancerSubnetName: 'Public subnet',
                    autoScalingSubnetName: 'Private subnet'
            ],

            environment: [
                    name: 'deploymentTest-env-prod',
                    type: 'LoadBalanced',
                    solutionStackName: '64bit Amazon Linux 2014.03 v1.0.4 running Tomcat 7 Java 7',
            ],

            application: [
                    healthCheckUrl: '/available'
            ],

            jvm: [
                    maxHeapSize: '512m',
                    maxPermSize: '512m',
                    initHeapSize: '256m'
            ],

            storage: [
                    warBucket: 'deployment-test-wars'
            ],

            transcoder: [
                    topicName: 'transcoder-notification-prod-test',
                    pipelineName: 'http-live-streaming-prod-test',

                    roleName: 'Transcoder-Test-Role',

                    inputBucket: 'master-videos-test',
                    outputBucket: 'playlist-and-segments-test',
            ]
    ]
}

Map loadSingleInstanceConfig() {
    [
            resetResources: true,

            launch: [
                    instanceProfileName: 'EC2-Instance-Test-Role',
                    securityGroupNames: ['single-ssl-test']
            ],

            environment: [
                    name: 'deploymentTest-env-more',
                    type: 'SingleInstance',
                    solutionStackName: '64bit Amazon Linux 2014.03 v1.0.4 running Tomcat 7 Java 7',
            ],

            application: [
                    healthCheckUrl: '/available'
            ],

            jvm: [
                    maxHeapSize: '512m',
                    maxPermSize: '512m',
                    initHeapSize: '256m'
            ],

            storage: [
                    warBucket: 'deployment-test-wars'
            ],

            transcoder: [
                    topicName: 'transcoder-notification-test',
                    pipelineName: 'http-live-streaming-test',

                    roleName: 'Transcoder-Test-Role',

                    inputBucket: 'master-videos-test',
                    outputBucket: 'playlist-and-segments-test',
            ]
    ]
}
