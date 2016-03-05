package in.reeltime.deployment.server

import groovy.transform.ToString
import org.gradle.api.GradleException
import org.gradle.api.Project
import static in.reeltime.deployment.log.StatusLogger.*

@ToString(includeFields = true, includePackage = false)
class LocalServer implements Server {

    @Delegate
    private Project project
    private Process process

    final String hostname = "localhost"
    final int port = 8080

    LocalServer(Project project) {
        this.project = project
    }

    void start() {
        String javaHome = System.getenv("JAVA_HOME")
        ProcessBuilder builder = new ProcessBuilder("${javaHome}/bin/java", "-Dgrails.env=dev", "-jar", "${buildDir}/libs/${name}-${version}.war")

        displayStatus("Starting local server process...")
        process = builder.start()

        final InputStream inputStream = process.inputStream
        new Thread(new Runnable() {
            void run() {
                File file = getLocalServerOutputLogFile()
                PrintWriter writer = new PrintWriter(file)

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))

                    String line
                    while ((line = reader.readLine()) != null) {
                        displayStatus("[SERVER] - " + line)
                        writer.println(line)
                    }
                }
                catch (IOException e) {
                    throw new GradleException("Error running server in background", e)
                }
                finally {
                    inputStream.close()
                }
            }
        }).start()
    }

    void stop() {
        displayStatus("Destroying local server process...")
        process.destroyForcibly()

        displayStatus("Waiting for server to shutdown...")
        process.waitFor()
    }

    private File getLocalServerOutputLogFile() {
        File dir = new File("$buildDir/reports/functional")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        new File(dir, 'local-server-output.log')
    }
}
