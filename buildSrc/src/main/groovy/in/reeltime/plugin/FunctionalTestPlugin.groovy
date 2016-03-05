package in.reeltime.plugin

import groovy.transform.CompileStatic
import in.reeltime.deployment.aws.AWSClientFactory
import in.reeltime.deployment.aws.client.EnhancedAWSElasticBeanstalk
import in.reeltime.deployment.configuration.DeploymentConfiguration
import in.reeltime.deployment.configuration.GrailsEnvironment
import in.reeltime.deployment.server.AcceptanceServer
import in.reeltime.deployment.server.LocalServer
import in.reeltime.deployment.server.ServerReachability
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import static in.reeltime.deployment.configuration.EnvironmentName.ACCEPTANCE

/**
 * Functional test plugin. Heavily based on the IntegrationTestPlugin from Grails core.
 */
class FunctionalTestPlugin implements Plugin<Project> {

    boolean  ideaIntegration = true
    String sourceFolderName = "src/functional-test"

    @Override
    void apply(Project project) {
        def sourceDirs = findFunctionalTestSources(project)

        if (!sourceDirs || sourceDirs.length == 0 as int) {
            return;
        }

        def acceptedSourceDirs = []

        project.with {
            sourceSets {
                functionalTest { sourceSet ->
                    sourceDirs.each { File srcDir ->
                        if (sourceSet.hasProperty(srcDir.name)) {
                            sourceSet."${srcDir.name}".srcDir srcDir
                            acceptedSourceDirs << srcDir
                        }
                    }
                }
            }

            dependencies {
                functionalTestCompile sourceSets.main.output
                functionalTestCompile sourceSets.test.output
                functionalTestCompile configurations.testCompile
                functionalTestRuntime configurations.testRuntime
            }

            task(type: Test, dependsOn: assemble, 'localFunctionalTest') {
                description = 'Runs the functional tests against the local server.'
                group = 'verification'

                testClassesDir = sourceSets.functionalTest.output.classesDir
                classpath = sourceSets.functionalTest.runtimeClasspath

                testLogging {
                    exceptionFormat = 'full'
                }

                outputs.upToDateWhen { false }

                shouldRunAfter integrationTest
                maxParallelForks = 1

                systemProperties = [
                        "in.reeltime.testing.environment": "local"
                ]

                reports {
                    html.destination = project.file("$html.destination/functional")
                    junitXml.destination = project.file("$junitXml.destination/functional")
                }

                LocalServer localServer = new LocalServer(project)

                Thread shutdownHook = new Thread({
                    localServer.stop()
                })

                doFirst {
                    localServer.start()
                    Runtime.getRuntime().addShutdownHook(shutdownHook)

                    if (!ServerReachability.waitUntilReachable(localServer)) {
                        throw new GradleException("Server was unreachable")
                    }
                }

                doLast {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook)
                    localServer.stop()
                }
            }

            check.dependsOn localFunctionalTest

            task(type: Test, 'acceptanceTest') {
                description = 'Runs the functional tests against the single instance AWS server.'
                group = 'verification'

                testClassesDir = sourceSets.functionalTest.output.classesDir
                classpath = sourceSets.functionalTest.runtimeClasspath

                testLogging {
                    exceptionFormat = 'full'
                }

                outputs.upToDateWhen { false }

                maxParallelForks = 1

                systemProperties = [
                        "in.reeltime.testing.environment": "acceptance",
                        "AWSAccessKey": System.properties["AWSAccessKey"],
                        "AWSSecretKey": System.properties["AWSSecretKey"]
                ]

                reports {
                    html.destination = project.file("$html.destination/acceptance")
                    junitXml.destination = project.file("$junitXml.destination/acceptance")
                }

                doFirst {
                    String environment = GrailsEnvironment.grailsEnv

                    if (environment != ACCEPTANCE) {
                        throw new GradleException("Acceptance tests can only be run in the acceptance environment")
                    }

                    DeploymentConfiguration deployConfig = new DeploymentConfiguration(environment)

                    AWSClientFactory clientFactory = new AWSClientFactory()
                    EnhancedAWSElasticBeanstalk eb = clientFactory.createEBClient()

                    AcceptanceServer acceptanceServer = new AcceptanceServer(project, deployConfig, eb)

                    if (!ServerReachability.waitUntilReachable(acceptanceServer)) {
                        throw new GradleException("Server was unreachable")
                    }
                }
            }

            if (ideaIntegration) {
                project.afterEvaluate {
                    if (project.convention.findByName('idea')) {
                        idea {
                            module {
                                acceptedSourceDirs.each {
                                    testSourceDirs += it
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @CompileStatic
    private File[] findFunctionalTestSources(Project project) {
        project.file(sourceFolderName).listFiles({File file-> file.isDirectory() && !file.name.contains('.')} as FileFilter)
    }
}
