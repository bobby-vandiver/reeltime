package in.reeltime.metadata

import groovy.json.JsonSlurper
import in.reeltime.exceptions.ProbeException

class FfprobeService {

    String ffprobe

    Map probeVideo(File video) {
        try {
            log.debug("Entering ${this.class.simpleName} probeVideo for video [${video.absolutePath}]")
            ensurePathToFfprobeIsDefined()

            def command = "${ffprobe} ${ffprobeLogLevel} -print_format json -show_streams ${video.absolutePath}"
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

    private void ensurePathToFfprobeIsDefined() {
        if(!ffprobe || !ffprobeExists())
            throw new IllegalStateException('ffprobe could not be found')
    }

    private void ffprobeExists() {
        new File(ffprobe).exists()
    }

    private String getFfprobeLogLevel() {
        String level = ''

        if(log.isFatalEnabled()) {
            level = 'fatal'
        }
        if(log.isErrorEnabled()) {
            level = 'error'
        }
        if(log.isWarnEnabled()) {
            level = 'warning'
        }
        if(log.isInfoEnabled()) {
            level = 'info'
        }
        if(log.isDebugEnabled()) {
            level = 'verbose'
        }
        else if(log.isTraceEnabled()) {
            level = 'debug'
        }

        return level.empty ? level : "-v $level"
    }
}
