import grails.util.Environment

grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]

grails.war.resources = { stagingDir, args ->
    // Package external applications, e.g. ffprobe
    copy(todir: "${stagingDir}/external") {
        fileset(dir: "${basedir}/external", excludes: "ffmpeg")
    }
    // Package AWS Elastic Beanstalk configuration
    copy(todir: "${stagingDir}/.ebextensions") {
        fileset(dir: "${basedir}/.ebextensions")
    }
    // Do not use the self-signed certificate for production
    if(Environment.currentEnvironment == Environment.PRODUCTION) {
        delete {
            fileset(file: "${stagingDir}/.ebextensions/00.singlessl.config")
        }
    }
}

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"

        mavenRepo "http://repo.spring.io/milestone/"

        mavenRepo "http://ec2-54-85-10-255.compute-1.amazonaws.com/artifactory/repo"

        // Repository for disable optimization JAR
        // Source: https://github.com/renataogarcia/disableOptimizationsTransformation/issues/1
        mavenRepo "https://raw.github.com/thecleancoder/mavenrepo/master/"
    }

    // Load credentials from a properties file
    // This is a work around for IntelliJ
    // Jenkins will provide credentials via system properties
    Properties artifactoryProperties = loadArtifactoryProperties()

    credentials {
        realm = "Artifactory Realm"
        host = "ec2-54-85-10-255.compute-1.amazonaws.com"
        username = System.getProperty('artifactoryUsername') ?: artifactoryProperties.get('username')
        password = System.getProperty('artifactoryPassword') ?: artifactoryProperties.get('password')
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.

        // runtime 'mysql:mysql-connector-java:5.1.22'

        compile 'commons-codec:commons-codec:1.9'
        compile 'com.amazonaws:aws-java-sdk:1.7.13'

        // FileUtils for test cleanup
        test 'commons-io:commons-io:2.4'

        test 'org.codehaus.groovy.modules.http-builder:http-builder:0.7.+', {
            excludes "commons-logging", "xml-apis", "groovy"
        }

        // Disables Groovy optimizations for accurate Cobertura reporting
        test 'com.github.renataogarcia:disable-groovy-compiler-optimizations-transformation:0.1-SNAPSHOT'
    }

    plugins {
        build ":tomcat:7.0.52.1"

        runtime ":hibernate:3.6.10.13"
        runtime ":jquery:1.8.3"
        runtime ":resources:1.2.7"
        runtime ":database-migration:1.4.0"

        compile ":scaffolding:2.0.3"
        compile ':cache:1.1.6'

        compile ":quartz:1.0.2"

        // TODO: Specify the new release once plugin is merged back into main code line
        compile ":spring-security-oauth2-provider:1.0.5-SNAPSHOT"
        compile ":spring-security-core:2.0-RC2"

        compile ":codenarc:0.21"

        test ":code-coverage:1.2.7"
        test ":functional-spock:0.7"
        test ":rest-client-builder:2.0.1"
    }
}

codenarc {

    // CodeNarc configuration for provided rules
    properties = {
        // Ensure static import statements come after non-static imports
        MisorderedStaticImports.comesBefore = false

        // IntelliJ will automatically squash multiple imports from the same package into a wildcard import
        NoWildcardImports.enabled = false
    }

    reports = {

        // Generate an HTML report for local viewing
        HtmlCodeNarcReport('html') {
            outputFile = 'target/codenarc.html'
            title = 'ReelTime CodeNarc Report'
        }

        // Generate an XML report for Jenkins to use for violation analysis
        XmlCodeNarcReport('xml') {
            outputFile = 'target/codenarc.xml'
            title = 'ReelTime CodeNarc Report'
        }
    }
}

private Properties loadArtifactoryProperties() {
    Properties artifactoryProperties = new Properties()
    String path = System.getProperty('user.home') + File.separator + '.grails' + File.separator + 'artifactory.properties'

    File file = new File(path)
    if(file.exists()) {
        artifactoryProperties.load(new FileInputStream(path))
    }
    return artifactoryProperties
}
