package in.reeltime.deployment.server

import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import groovy.transform.ToString
import in.reeltime.deployment.aws.client.EnhancedAWSElasticBeanstalk
import in.reeltime.deployment.configuration.ApplicationConfiguration
import in.reeltime.deployment.configuration.DeploymentConfiguration
import org.gradle.api.Project

@ToString(includeFields = true, includePackage = false)
class AcceptanceServer implements Server {

    private String applicationName
    private String applicationVersion

    private String hostname

    private String environmentName
    private EnhancedAWSElasticBeanstalk eb

    AcceptanceServer(Project project, DeploymentConfiguration deployConfig, EnhancedAWSElasticBeanstalk eb) {
        this.eb = eb
        this.environmentName = deployConfig.configuration.environment.name
        this.applicationName = ApplicationConfiguration.APPLICATION_NAME
        this.applicationVersion = project.version + '-' + deployConfig.environment
    }

    @Override
    String getHostname() {
        if (!hostname) {
            EnvironmentDescription environment = getEnvironment()
            hostname = environment ? environment.CNAME : null
        }
        return hostname
    }

    @Override
    int getPort() {
        return 80
    }

    private EnvironmentDescription getEnvironment() {
        eb.findEnvironment(applicationName, environmentName)
    }
}
