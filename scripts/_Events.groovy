import grails.util.Environment
import org.apache.commons.io.FileUtils

includeTargets << grailsScript("_GrailsPackage")

eventTestPhasesEnd = {

    // TODO: Remove this once we set Jenkins up on a remote server
    // This is only necessary while Jenkins and I share the same temporary directory
    if(Environment.current == Environment.TEST) {
        if(!binding.hasProperty('config')) {
            createConfig()
        }
        def inputPath = config.reeltime.storage.input
        deleteDirectory(inputPath)

        def outputPath = config.reeltime.storage.output
        deleteDirectory(outputPath)
    }
}

private deleteDirectory(String path) {
    def directory = new File(path)
    try {
        println "Deleting storage directory: $path"
        FileUtils.forceDelete(directory)
    }
    catch(IOException e) {
        println "Could not delete directory: ${e}"
    }
}
