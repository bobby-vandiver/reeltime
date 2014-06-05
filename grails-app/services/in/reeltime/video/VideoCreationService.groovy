package in.reeltime.video

import in.reeltime.metadata.StreamMetadata
import in.reeltime.user.User

class VideoCreationService {

    def pathGenerationService
    def inputStorageService

    def transcoderService
    def streamMetadataService

    def grailsApplication

    private static final DURATION_FORMAT = /(\d+)\.(\d+)/

    boolean canCreate(VideoCreationCommand command) {

        def temp = writeVideoStreamToTempFile(command)
        def streams = streamMetadataService.extractStreams(temp)
        return !containsInvalidStream(streams)
    }

    private File writeVideoStreamToTempFile(VideoCreationCommand command) {
        def temp = File.createTempFile('can-create-video', '.tmp')
        return temp
    }

    private boolean containsInvalidStream(List<StreamMetadata> streams) {
        streams.each { stream ->
            if(invalidDuration(stream)) {
                return false
            }
        }
        return true
    }

    private boolean invalidDuration(StreamMetadata stream) {
        def duration = stream.duration
        invalidDurationFormat(duration) || exceedsMaxDuration(duration)
    }

    private boolean invalidDurationFormat(String duration) {
        !(duration ==~ DURATION_FORMAT)
    }

    private boolean exceedsMaxDuration(String duration) {
        def maxDuration = grailsApplication.config.reeltime.metadata.maxDurationInSeconds
        def matcher = (duration =~ DURATION_FORMAT)

        def seconds = matcher[0][1] as int
        return seconds >= maxDuration
    }

    def createVideo(User creator, String title, InputStream videoStream) {

        def masterPath = pathGenerationService.uniqueInputPath
        inputStorageService.store(videoStream, masterPath)

        def video = new Video(creator: creator, title: title, masterPath: masterPath).save()
        log.info("Created video with id [${video.id}] for user [${creator.username}]")

        def outputPath = pathGenerationService.uniqueOutputPath
        transcoderService.transcode(video, outputPath)

        return video
    }
}
