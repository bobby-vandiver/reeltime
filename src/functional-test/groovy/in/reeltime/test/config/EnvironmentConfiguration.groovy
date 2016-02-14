package in.reeltime.test.config

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import groovy.transform.CompileStatic

@CompileStatic
class EnvironmentConfiguration {

    private static String baseUrl
    private static String environmentName

    static boolean isLocalEnvironment() {
        return getEnvironmentName() == "local"
    }

    static boolean isAcceptanceEnvironment() {
        return getEnvironmentName() == "acceptance"
    }

    static synchronized String getBaseUrl() {
        if (!baseUrl) {
            if (isLocalEnvironment()) {
                baseUrl = "http://localhost:8080/"
            }
            else if (isAcceptanceEnvironment()) {
                AWSCredentials credentials = loadAWSCredentials()
                AWSElasticBeanstalk eb = new AWSElasticBeanstalkClient(credentials)

                EnvironmentDescription awsEnvironment = findAcceptanceEnvironment(eb)
                baseUrl = awsEnvironment ? "http://" + awsEnvironment.CNAME + "/" : null
            }
        }
        return baseUrl
    }

    private static synchronized String getEnvironmentName() {
        if (!environmentName) {
            environmentName = System.properties["in.reeltime.testing.environment"]
        }
        return environmentName
    }

    private static AWSCredentials loadAWSCredentials() {
        String accessKey = System.properties["AWSAccessKey"]
        String secretKey = System.properties["AWSSecretKey"]
        new BasicAWSCredentials(accessKey, secretKey)
    }

    private static EnvironmentDescription findAcceptanceEnvironment(AWSElasticBeanstalk eb) {
        return eb.describeEnvironments().environments.find {
            it.applicationName == "ReelTime" && it.environmentName == "reeltime-acceptance"
        }
    }
}
