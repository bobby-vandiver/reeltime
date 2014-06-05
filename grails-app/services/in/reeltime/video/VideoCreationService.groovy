package in.reeltime.video

import in.reeltime.metadata.StreamMetadata
import in.reeltime.user.User

class VideoCreationService {

    def pathGenerationService
    def inputStorageService

    def transcoderService
    def streamMetadataService

    boolean allowCreation(VideoCreationCommand command) {

        def temp = writeVideoStreamToTempFile(command)
        def streams = streamMetadataService.extractStreams(temp)
        // TODO: Reload input stream from temp file
        return validateStreams(streams)
    }

    private static File writeVideoStreamToTempFile(VideoCreationCommand command) {
        def temp = File.createTempFile('can-create-video', '.tmp')
        return temp
    }

    private static boolean validateStreams(List<StreamMetadata> streams) {
        streams.find { !it.validate() } == null
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
