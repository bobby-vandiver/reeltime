import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription

import java.text.SimpleDateFormat

includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsBootstrap")

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

    String baseUrl = "http://${environment.CNAME}/"
    displayStatus("Executing functional tests against: $baseUrl")

    String grailsCommand = windows ? 'grailsw.bat' : './grailsw'

    // This isn't the ideal way to execute the tests, since we're starting another Grails process,
    // but attempting to launch the tests via the allTests() target defined in the _GrailsTests
    // script results in a ClassNotFoundException for the Spock class loader.
    //
    // This seems to be related to GRAILS-6453:
    // http://jira.grails.org/browse/GRAILS-6453

    def process = "${grailsCommand} test-app functional -baseUrl=$baseUrl --stacktrace".execute()

    def errorGobbler = new StreamGobbler(is: process.err)
    def outputGobbler = new StreamGobbler(is: process.in)

    errorGobbler.start()
    outputGobbler.start()

    def exitCode = process.waitFor()
    System.exit(exitCode)
}

boolean isWindows() {
    System.getProperty("os.name").startsWith('Windows')
}

// Source: http://stackoverflow.com/questions/1732455/redirect-process-output-to-stdout
class StreamGobbler extends Thread {
    InputStream is;

    void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is)
            BufferedReader br = new BufferedReader(isr)

            String line
            while ( (line = br.readLine()) != null) {
                println "[${timestamp()}] $line"
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // Unfortunate copy/pasta to ensure consistent logging for "gobbled" output
    private static String timestamp() {
        def dateFormat = new SimpleDateFormat('EEE MMM d yyyy HH:mm:ss')
        dateFormat.format(new Date())
    }
}


setDefaultTarget(acceptanceTests)
