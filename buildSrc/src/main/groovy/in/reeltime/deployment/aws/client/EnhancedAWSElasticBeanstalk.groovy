package in.reeltime.deployment.aws.client

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription
import com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription

class EnhancedAWSElasticBeanstalk implements AWSElasticBeanstalk {

    private static final String TERMINATED = 'Terminated'
    private static final String READY = 'Ready'

    @Delegate
    AWSElasticBeanstalk awsElasticBeanstalk

    EnhancedAWSElasticBeanstalk(AWSElasticBeanstalk awsElasticBeanstalk) {
        this.awsElasticBeanstalk = awsElasticBeanstalk
    }

    boolean applicationExists(String applicationName) {
        ApplicationDescription application = describeApplications().applications.find {
            it.applicationName == applicationName
        }
        return application != null
    }

    boolean applicationVersionExists(String applicationName, String version) {
        ApplicationVersionDescription applicationVersion = describeApplicationVersions().applicationVersions.find {
            it.applicationName == applicationName && it.versionLabel == version
        }

        return applicationVersion != null
    }

    EnvironmentDescription findEnvironment(String applicationName, String environmentName) {
        return describeEnvironments().environments.find {
            it.applicationName == applicationName && it.environmentName == environmentName
        }
    }

    EnvironmentDescription findEnvironmentByVersion(String applicationName, String environmentName, String version) {
        return describeEnvironments().environments.find {
            it.applicationName == applicationName && it.environmentName == environmentName && it.versionLabel == version
        }
    }

    boolean environmentExists(String applicationName, String environmentName) {
        EnvironmentDescription environment = findEnvironment(applicationName, environmentName)
        return environment != null && environment.status != TERMINATED
    }

    boolean environmentExistsForVersion(String applicationName, String environmentName, String version) {
        EnvironmentDescription environment = findEnvironmentByVersion(applicationName, environmentName, version)
        return environment != null && environment.status != TERMINATED
    }

    boolean environmentIsReady(String applicationName, String environmentName, String version) {
        EnvironmentDescription environment = findEnvironmentByVersion(applicationName, environmentName, version)
        return environment != null && environment.status != READY
    }
}
