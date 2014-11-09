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

targetEnvironmentIsLoadBalanced = {
    return deployConfig.loadBalancer != null
}

targetEnvironmentIsProduction = {
    return grailsEnvironmentName() == 'production'
}

// TODO: Refactor common options that are not environment specific, e.g. solution stack
Map loadProductionConfig() {
    [
            resetResources: false,

            launch: [
                    instanceProfileName: 'EC2-Instance-Production-Role',
                    securityGroup: 'NAT-SG'
            ],

            vpc: [
                    vpcId: 'vpc-cac96baf',
                    loadBalancerSubnetName: 'Public subnet',
                    autoScalingSubnetName: 'Private subnet'
            ],

            loadBalancer: [
                    certificateName: 'single-instance-certificate'
            ],

            environment: [
                    name: 'reeltime-production',
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
                    warBucket: 'reeltime-deployment-wars'
            ],

            transcoder: [
                    topicName: 'transcoder-notification-production',
                    pipelineName: 'http-live-streaming-production',

                    roleName: 'Transcoder-Production-Role',

                    inputBucket: 'master-videos-production',
                    outputBucket: 'playlists-and-segments-production',
            ]
    ]
}

Map loadSingleInstanceConfig() {
    [
            resetResources: true,

            launch: [
                    instanceProfileName: 'EC2-Instance-Acceptance-Role',
            ],

            postLaunch: [
                    securityGroups: ['SNS-HTTP-Access-US-and-EU', 'SNS-HTTP-Access-AP-and-SA',
                                     'Internal-Developer-Only', 'Demo-Access']
            ],

            environment: [
                    name: 'reeltime-acceptance',
                    type: 'SingleInstance',

                    // TODO: Change to 64bit Amazon Linux 2014.09 v1.0.9 running Tomcat 7 Java 7
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
                    warBucket: 'reeltime-deployment-wars'
            ],

            transcoder: [
                    topicName: 'transcoder-notification-acceptance',
                    pipelineName: 'http-live-streaming-acceptance',

                    roleName: 'Transcoder-Acceptance-Role',

                    inputBucket: 'master-videos-acceptance',
                    outputBucket: 'playlists-and-segments-acceptance',
            ]
    ]
}
