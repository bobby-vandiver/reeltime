package in.reeltime.transcoder

import grails.plugin.spock.IntegrationSpec
import in.reeltime.transcoder.local.FfmpegTranscoderService
import in.reeltime.video.Video
import org.apache.commons.io.FileUtils
import spock.lang.IgnoreIf

class FfmpegTranscoderServiceIntegrationSpec extends IntegrationSpec {

    FfmpegTranscoderService service

    def localFileSystemService
    def grailsApplication

    def pathGenerationService
    def inputStorageService

    void setup() {
        service = new FfmpegTranscoderService()
        service.grailsApplication = grailsApplication
        service.localFileSystemService = localFileSystemService
    }

    @IgnoreIf({!System.getProperty('ffmpeg') && !System.getenv('FFMPEG')})
    void "transcode video file using ffmpeg"() {
        given:
        def masterPath = pathGenerationService.uniqueInputPath
        def video = new Video(title: 'change peter parker', masterPath:  masterPath).save()

        and:
        def videoFilePath = 'test/files/spidey.mp4'
        storeTestVideo(masterPath, videoFilePath)

        and:
        def outputPath = pathGenerationService.uniqueOutputPath

        when:
        service.transcode(video, outputPath)

        then:
        assertDirectoryContainsPlaylistAndSegments(outputPath)

        cleanup:
        def outputDirectoryPath = grailsApplication.config.reeltime.storage.output as String
        FileUtils.deleteDirectory(new File(outputDirectoryPath))
    }

    private void storeTestVideo(String storagePath, String filePath) {
        new File(filePath).withInputStream { videoStream ->
            inputStorageService.store(videoStream, storagePath)
        }
    }

    private void assertDirectoryContainsPlaylistAndSegments(String path) {
        def output = grailsApplication.config.reeltime.storage.output
        def fullPath = "$output${File.separator}$path"

        def directory = new File(fullPath)
        assert directory.isDirectory()
        assert directory.listFiles().find { it.name.endsWith('.m3u8') }
        assert directory.listFiles().count { it.name.endsWith('.ts') } > 0
    }
}
