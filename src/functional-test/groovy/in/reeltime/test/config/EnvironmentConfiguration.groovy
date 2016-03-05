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

    static boolean isRemoteEnvironment() {
        return getEnvironmentName() == "remote"
    }

    static synchronized String getBaseUrl() {
        if (!baseUrl) {
            if (isLocalEnvironment()) {
                baseUrl = "http://localhost:8080/"
            }
            else if (isRemoteEnvironment()) {
                String protocol = System.properties["in.reeltime.testing.remote.protocol"]
                String hostname = System.properties["in.reeltime.testing.remote.hostname"]
                String port = System.properties["in.reeltime.testing.remote.port"]
                baseUrl = "$protocol://$hostname:$port/"
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
}
