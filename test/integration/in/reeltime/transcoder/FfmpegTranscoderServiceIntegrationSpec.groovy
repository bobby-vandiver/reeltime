package in.reeltime.transcoder

import grails.test.spock.IntegrationSpec
import in.reeltime.video.Video
import org.apache.tika.Tika
import spock.lang.IgnoreIf
import test.helper.UserFactory

class FfmpegTranscoderServiceIntegrationSpec extends IntegrationSpec {

    def ffmpegTranscoderService

    def grailsApplication

    def playlistAndSegmentStorageService
    def videoStorageService
    def thumbnailStorageService

    @IgnoreIf({!System.getProperty('FFMPEG') && !System.getenv('FFMPEG')})
    void "transcode video file using ffmpeg"() {
        given:
        def creator = UserFactory.createTestUser()

        def masterPath = videoStorageService.uniqueVideoPath
        def masterThumbnailPath = thumbnailStorageService.uniqueThumbnailPath

        def video = new Video(creator: creator, title: 'change peter parker',
                masterPath: masterPath, masterThumbnailPath: masterThumbnailPath).save()

        and:
        def videoFilePath = 'test/files/videos/spidey.mp4'
        storeTestVideo(masterPath, videoFilePath)

        and:
        def thumbnailFilePath = 'test/files/images/small.png'
        storeTestThumbnail(masterThumbnailPath, thumbnailFilePath)

        and:
        def outputPath = playlistAndSegmentStorageService.uniquePlaylistPath

        when:
        ffmpegTranscoderService.transcode(video, outputPath)

        then:
        video.available
        video.playlists.size() == 1

        and:
        assertDirectoryContainsThumbnails(video)
        assertDirectoryContainsPlaylistAndSegments(outputPath)

        cleanup:
        creator.delete()

        and:
        removeOutputDirectories()
    }

    private void storeTestVideo(String storagePath, String filePath) {
        new File(filePath).withInputStream { videoStream ->
            videoStorageService.store(videoStream, storagePath)
        }
    }

    private void storeTestThumbnail(String storagePath, String filePath) {
        new File(filePath).withInputStream { thumbnailStream ->
            thumbnailStorageService.store(thumbnailStream, storagePath)
        }
    }

    private void assertDirectoryContainsThumbnails(Video video) {
        def output = grailsApplication.config.reeltime.storage.thumbnails

        assert video.thumbnails.size() == 3

        video.thumbnails.each { thumbnail ->
            def fullPath = "${output}${File.separator}${thumbnail.uri}"

            def file = new File(fullPath)
            assert file.exists()

            def tika = new Tika()
            def mimeType = tika.detect(file)
            assert mimeType == 'image/png'
        }
    }

    private void assertDirectoryContainsPlaylistAndSegments(String path) {
        def output = grailsApplication.config.reeltime.storage.playlists
        def fullPath = "$output${File.separator}$path"

        def directory = new File(fullPath)
        assert directory.isDirectory()
        assert directory.listFiles().find { it.name.endsWith('.m3u8') }
        assert directory.listFiles().count { it.name.endsWith('.ts') } > 0
    }

    private void removeOutputDirectories() {
        def playlistsPath = grailsApplication.config.reeltime.storage.playlists as String
        new File(playlistsPath).deleteOnExit()

        def thumbnailsPath = grailsApplication.config.reeltime.storage.thumbnails as String
        new File(thumbnailsPath).deleteOnExit()
    }
}
