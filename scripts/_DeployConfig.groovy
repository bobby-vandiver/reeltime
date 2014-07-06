import grails.util.Environment

target(loadDeployConfig: "Populates the deployConfig map based on the current Grails environment") {

    def currentEnvironment = Environment.currentEnvironment.name
    if(currentEnvironment == 'production') {
        deployConfig = loadProductionConfig()
    }
    else {
        // TODO: Create proper staging environment to mirror production
        deployConfig = loadStagingConfig()
    }
}

Map loadProductionConfig() {
    [
            launch: [
                    instanceProfileName: 'aws-elasticbeanstalk-ec2-role'
            ],

            environment: [
                    name: 'deploymentTest-env-prod',
                    type: 'SingleInstance',
                    solutionStackName: '64bit Amazon Linux 2014.03 v1.0.4 running Tomcat 7 Java 7',
            ],

            jvm: [
                    maxHeapSize: '512m',
                    maxPermSize: '512m',
                    initHeapSize: '256m'
            ],

            storage: [
                    warBucket: 'deployment-test-wars'
            ]
    ]
}

Map loadStagingConfig() {
    [
            launch: [
                    instanceProfileName: 'aws-elasticbeanstalk-ec2-role'
            ],

            environment: [
                    name: 'deploymentTest-env-more',
                    type: 'SingleInstance',
                    solutionStackName: '64bit Amazon Linux 2014.03 v1.0.4 running Tomcat 7 Java 7',
            ],

            jvm: [
                    maxHeapSize: '512m',
                    maxPermSize: '512m',
                    initHeapSize: '256m'
            ],

            storage: [
                    warBucket: 'deployment-test-wars'
            ]
    ]
}
