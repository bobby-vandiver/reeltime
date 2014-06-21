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
        def path = config.reeltime.storage.output
        def outputDirectory = new File(path)

        try {
            println "Deleting storage output directory: $path"
            FileUtils.forceDelete(outputDirectory)
        }
        catch(IOException e) {
            println "Could not delete output directory: ${e}"
        }
    }
}
