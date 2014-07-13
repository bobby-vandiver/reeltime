import grails.util.Environment

includeTargets << new File("${basedir}/scripts/_Common.groovy")

target(loadDeployConfig: "Populates the deployConfig map based on the current Grails environment") {

    def currentEnvironment = Environment.currentEnvironment.name
    displayStatus("Loading deployment configuration for environment [$currentEnvironment]")

    if(currentEnvironment == 'production') {
        deployConfig = loadProductionConfig()
    }
    else {
        // TODO: Create proper staging environment to mirror production
        deployConfig = loadSingleInstanceConfig()
    }
}

resetResourcesIsAllowed = {
    return deployConfig.resetResources == true
}

// TODO: Refactor common options that are not environment specific, e.g. solution stack
// TODO: Production config should be load balanced
Map loadProductionConfig() {
    [
            resetResources: false,

            launch: [
                    instanceProfileName: 'EC2-Instance-Test-Role',
                    securityGroupName: 'single-ssl-test'
            ],

            environment: [
                    name: 'deploymentTest-env-prod',
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
                    topicName: 'transcoder-notification-prod-test',
                    pipelineName: 'http-live-streaming-prod-test',

                    roleName: 'elasticTranscoder-prod-test',

                    inputBucket: 'master-videos-prod-test',
                    outputBucket: 'playlist-and-segments-prod-test',
            ]
    ]
}

Map loadSingleInstanceConfig() {
    [
            resetResources: true,

            launch: [
                    instanceProfileName: 'EC2-Instance-Test-Role',
                    securityGroupName: 'single-ssl-test'
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

                    roleName: 'elasticTranscoder-test',

                    inputBucket: 'master-videos-test',
                    outputBucket: 'playlist-and-segments-test',
            ]
    ]
}
