package in.reeltime.plugin

import groovy.transform.CompileStatic
import in.reeltime.deployment.server.LocalServer
import in.reeltime.deployment.server.RemoteServer
import in.reeltime.deployment.server.ServerReachability
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

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
                    html.destination = project.file("$html.destination/localFunctional")
                    junitXml.destination = project.file("$junitXml.destination/localFunctional")
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

            task(type: Test, 'remoteFunctionalTest') {
                description = 'Runs the functional tests against a remote server.'
                group = 'verification'

                testClassesDir = sourceSets.functionalTest.output.classesDir
                classpath = sourceSets.functionalTest.runtimeClasspath

                testLogging {
                    exceptionFormat = 'full'
                }

                outputs.upToDateWhen { false }

                maxParallelForks = 1

                systemProperties = [
                        "in.reeltime.testing.environment": "remote",
                        "in.reeltime.testing.remote.hostname": System.getProperty("REMOTE_HOSTNAME"),
                        "in.reeltime.testing.remote.port": System.getProperty("REMOTE_PORT"),
                        "in.reeltime.testing.remote.protocol": System.getProperty("REMOTE_PROTOCOL"),
                ]

                reports {
                    html.destination = project.file("$html.destination/remoteFunctional")
                    junitXml.destination = project.file("$junitXml.destination/remoteFunctional")
                }

                doFirst {
                    if (!System.getProperty("REMOTE_HOSTNAME") || !System.getProperty("REMOTE_PORT") || !System.getProperty("REMOTE_PROTOCOL")) {
                        throw new GradleException("usage: remoteFunctionalTest -DREMOTE_HOSTNAME=remoteHost -DREMOTE_PORT=remotePort -DREMOTE_PROTOCOL=https")
                    }

                    String hostname = System.getProperty("REMOTE_HOSTNAME")
                    int port = System.getProperty("REMOTE_PORT") as int

                    RemoteServer remoteServer = new RemoteServer(hostname, port)

                    if (!ServerReachability.waitUntilReachable(remoteServer)) {
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
