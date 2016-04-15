package in.reeltime.metadata

import grails.transaction.Transactional
import groovy.json.JsonSlurper
import in.reeltime.exceptions.ProbeException

@Transactional
class FfprobeService {

    String ffprobe

    Map probeVideo(File video) {
        try {
            ensureVideoExists(video)
            ensurePathToFfprobeIsDefined()

            log.debug("Probing video [${video.absolutePath}]")

            def command = "${ffprobe} ${ffprobeLogLevel} -print_format json -show_streams ${video.absolutePath}"
            log.debug("Executing command: $command")

            def process = command.execute()
            process.waitFor()

            log.info("Finished probing video [${video.path}]")

            def jsonText = process.in.text
            log.debug("jsonText from ffprobe: $jsonText")

            def errorText = process.err.text
            log.debug("error text: $errorText")

            new JsonSlurper().parseText(jsonText)
        }
        catch(Exception e) {
            throw new ProbeException(e)
        }
    }

    private void ensureVideoExists(File video) {
        if (!video || !video.exists()) {
            throw new IllegalArgumentException("Unknown video file [${video?.absolutePath}]")
        }
    }

    private void ensurePathToFfprobeIsDefined() {
        log.debug("Checking ffprobe path [$ffprobe]")
        if(!ffprobe || !ffprobeExists())
            throw new IllegalStateException('ffprobe could not be found')
    }

    private boolean ffprobeExists() {
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
