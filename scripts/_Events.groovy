import grails.util.Environment
import org.apache.commons.io.FileUtils

eventTestPhasesEnd = {

    // TODO: Remove this once we set Jenkins up on a remote server
    // This is only necessary while Jenkins and I share the same temporary directory
    if(Environment.current == Environment.TEST) {
        def path = grailsApp.config.reeltime.storage.output
        def outputDirectory = new File(path)

        println "Deleting storage output directory: $path"
        FileUtils.forceDelete(outputDirectory)
    }
}