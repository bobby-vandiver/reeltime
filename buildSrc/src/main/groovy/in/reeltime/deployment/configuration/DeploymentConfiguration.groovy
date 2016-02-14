package in.reeltime.deployment.configuration

import static in.reeltime.deployment.configuration.EnvironmentName.*

class DeploymentConfiguration {

    final Map configuration
    String environment

    DeploymentConfiguration(String environment) {
        this.environment = environment

        if (environment == PRODUCTION) {
            configuration = loadProductionConfig()
        }
        else if (environment == ACCEPTANCE) {
            configuration = loadSingleInstanceConfig()
        }
        else {
            configuration = null
            throw new IllegalArgumentException("Deployment from environment [$environment] is not supported")
        }
    }

    boolean resetResourcesIsAllowed() {
        return configuration.resetResources == true
    }

    boolean targetEnvironmentIsInVpc() {
        return configuration.vpc != null
    }

    boolean targetEnvironmentIsLoadBalanced() {
        return configuration.loadBalancer != null
    }

    boolean targetEnvironmentIsProduction() {
        return environment == PRODUCTION
    }

    // TODO: Refactor common options that are not environment specific, e.g. solution stack
    private static Map loadProductionConfig() {
        [
                resetResources: false,

                launch: [
                        instanceProfileName: 'EC2-Instance-Production-Role',
                        instanceType: 't2.micro',
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
                        solutionStackName: '64bit Amazon Linux 2015.03 v1.3.1 running Tomcat 8 Java 8',
                ],

                application: [
                        healthCheckUrl: '/aws/available'
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

    private static Map loadSingleInstanceConfig() {
        [
                resetResources: true,

                launch: [
                        instanceProfileName: 'EC2-Instance-Acceptance-Role',
                        instanceType: 't2.micro',
                ],

                postLaunch: [
                        securityGroups: ['SNS-HTTP-Access-US-and-EU', 'SNS-HTTP-Access-AP-and-SA',
                                         'Internal-Developer-Only', 'Demo-Access']
                ],

                environment: [
                        name: 'reeltime-acceptance',
                        type: 'SingleInstance',
                        solutionStackName: '64bit Amazon Linux 2015.03 v1.3.1 running Tomcat 8 Java 8',
                ],

                application: [
                        healthCheckUrl: '/aws/available'
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

}
