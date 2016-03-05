if (args.size() != 1 || (args[0] != "start" && args[0] != "stop")) {
    println "usage: [start|stop]"
    System.exit(1)
}

def containerIdFile = new File("mysql-container-id")

if (args[0] == "start") {
    def start = "docker run -d --name local-mysql -e MYSQL_ROOT_PASSWORD=mysql -e MYSQL_DATABASE=reeltime -p 3306:3306 mysql:5.6"

    exec(start, "Failed to run container!") { proc ->
        def container = proc.in.text.replace("\n", "").replace("\r", "").trim()

        containerIdFile.withWriter { writer ->
            writer.write(container)
            writer.newLine()
        }

        println "Container $container was successfully started"
    }
}
else if (args[0] == "stop") {
    def container

    containerIdFile.withReader { reader ->
        container = reader.readLine()
    }

    def stop  = "docker stop $container"
    def rm = "docker rm $container"

    exec(stop, "Failed to stop container $container") {
        exec(rm, "Failed to remove container $container") {
            println "Container $container was stopped and removed"

            if (!containerIdFile.delete()) {
                println "Failed to delete file ${containerIdFile.name}"
            }
        }
    }
}

private void exec(String command, String failureMessage, Closure successCallback) {
    def proc = command.execute()
    proc.waitFor()

    if (proc.exitValue() == 0) {
        successCallback(proc)
    }
    else {
        println failureMessage
        println "Process exit code: ${proc.exitValue()}"
        println "Std Err: ${proc.err.text}"
        println "Std Out: ${proc.in.text}"
    }
}
