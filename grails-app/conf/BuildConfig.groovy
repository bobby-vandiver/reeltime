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
        compile 'com.amazonaws:aws-java-sdk:1.7.+'

        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"

        test 'cglib:cglib-nodep:2.2.2'
        test 'org.objenesis:objenesis:1.4'

        // FileUtils for test cleanup
        test 'commons-io:commons-io:2.4'
    }

    plugins {
        build ":tomcat:7.0.52.1"

        runtime ":hibernate:3.6.10.13"
        runtime ":jquery:1.8.3"
        runtime ":resources:1.2.7"
        runtime ":database-migration:1.4.0"

        compile ":scaffolding:2.0.3"
        compile ':cache:1.1.6'

        // TODO: Specify the new release once plugin is merged back into main code line
        compile ":spring-security-oauth2-provider:1.0.5-SNAPSHOT"
        compile ":spring-security-core:2.0-RC2"

        test ":code-coverage:1.2.7"
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