import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription

includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsTest")

includeTargets << new File("${basedir}/scripts/_DeployConfig.groovy")
includeTargets << new File("${basedir}/scripts/_Common.groovy")
includeTargets << new File("${basedir}/scripts/_AwsClients.groovy")

target(acceptanceTests: "Executes functional tests against the acceptance environment") {
    depends(loadDeployConfig, initAwsClients, compile, classpath)

    if(grailsEnvironmentName() != 'acceptance') {
        displayStatus("Acceptance tests can only be run in the acceptance environment")
        System.exit(1)
    }

    String applicationName = applicationName()
    String environmentName = deployConfig.environment.name

    EnvironmentDescription environment = eb.findEnvironment(applicationName, environmentName)

    if(!environment) {
        displayStatus("Could not find environment [$environmentName] for application [$applicationName]")
        System.exit(1)
    }

    String baseUrl = "http://" + environment.CNAME + "/"
    displayStatus("Executing functional tests against: $baseUrl")

    argsMap.params << "functional:"
    argsMap.baseUrl = baseUrl

    allTests()
}

setDefaultTarget(acceptanceTests)
