package in.reeltime.metadata

import groovy.json.JsonSlurper
import in.reeltime.exceptions.ProbeException

class FfprobeService {

    def ffprobe

    Map probeVideo(File video) {
        try {
            log.debug("Entering ${this.class.simpleName} probeVideo for video [${video.absolutePath}]")
            ensurePathToFfprobeIsDefined(ffprobe)

            def command = "${ffprobe} -v quiet -print_format json -show_streams ${video.absolutePath}"
            log.debug("Executing command: $command")

            def process = command.execute()
            process.waitFor()

            log.info("Finished probing video [${video.path}]")

            def jsonText = process.in.text
            log.debug("jsonText from ffprobe: $jsonText")

            new JsonSlurper().parseText(jsonText)
        }
        catch(Exception e) {
            throw new ProbeException(e)
        }
    }

    private static void ensurePathToFfprobeIsDefined(String ffprobe) {
        if(!ffprobe || pathIsInvalid(ffprobe))
            throw new IllegalStateException('ffprobe could not be found')
    }

    private static boolean pathIsInvalid(String ffprobe) {
        !new File(ffprobe).exists()
    }
}
