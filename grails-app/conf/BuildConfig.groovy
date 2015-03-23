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

        // Repository for Spring Security RC plugin
        mavenRepo "http://repo.spring.io/milestone/"

        // Repository for disable optimization JAR
        // Source: https://github.com/renataogarcia/disableOptimizationsTransformation/issues/1
        mavenRepo "https://raw.github.com/thecleancoder/mavenrepo/master/"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.

        runtime 'mysql:mysql-connector-java:5.1.34'

        compile 'commons-codec:commons-codec:1.9'
        compile 'com.amazonaws:aws-java-sdk:1.7.13'

        compile 'org.apache.tika:tika-parsers:1.7'
        compile 'org.imgscalr:imgscalr-lib:4.2'

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

        runtime ":hibernate:3.6.10.18"
        runtime ":jquery:1.8.3"
        runtime ":resources:1.2.7"
        runtime ":database-migration:1.4.0"

        compile ":scaffolding:2.0.3"
        compile ':cache:1.1.6'

        compile ":quartz:1.0.2"

        compile ":spring-security-oauth2-provider:2.0-RC3"
        compile ":spring-security-core:2.0-RC4"

        compile ":codenarc:0.21"

        // This has to be a compile time dependency otherwise we will be unable to start
        // functional tests from Gant scripts.
        //
        // The exception thrown:
        // java.lang.ClassNotFoundException: grails.plugin.functional.spock.SpecTestTypeLoader
        compile ":functional-spock:0.7"

        test ":code-coverage:1.2.7"
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

        // The instances that Exception is caught is for wrapping and rethrowing
        CatchException.enabled = false

        // Returning null from a catch block is done intentionally in order to allow Grails to perform validation
        ReturnNullFromCatchBlock.enabled = false

        // Violations of this rules are for local development purposes (InMemoryMailService) or
        // the violation occurs as a result of properties being injected at runtime
        GrailsStatelessService.enabled = false

        // The flagged field names are not reserved by H2 or MySQL
        GrailsDomainReservedSqlKeywordName.doNotApplyToClassNames='ResourceRemovalTarget,AccessToken,RefreshToken'

        // The Client and User need access to the springSecurityService for encoding client secrets and passwords
        GrailsDomainWithServiceReference.doNotApplyToClassNames='Client,User'

        // Exclude classes provided by the Spring Security Core or OAuth2 plugin
        GrailsDomainHasEquals.doNotApplyToClassNames='AccessToken, AuthorizationCode, Client, RefreshToken, User, Role, UserRole'
        GrailsDomainHasToString.doNotApplyToClassNames='AccessToken, AuthorizationCode, Client, RefreshToken, User, Role, UserRole'

        // The UserRole class is provided by Spring Security Core and we do not want to modify the provided methods
        UnusedMethodParameter.doNotApplyToClassNames='UserRole'
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
