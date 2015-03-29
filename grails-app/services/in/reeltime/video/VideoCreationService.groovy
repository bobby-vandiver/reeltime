package in.reeltime.video

import in.reeltime.metadata.StreamMetadata

class VideoCreationService {

    def videoService
    def videoStorageService
    def reelVideoManagementService

    def thumbnailService
    def thumbnailStorageService
    def thumbnailValidationService

    def playlistService
    def playlistAndSegmentStorageService
    def streamMetadataService

    def transcoderService
    def transcoderJobService

    def temporaryFileService

    def maxVideoStreamSizeInBytes
    def maxThumbnailStreamSizeInBytes

    boolean allowCreation(VideoCreationCommand command) {
        validateVideoStream(command)
        validateThumbnailStream(command)
        return command.validate()
    }

    private void validateVideoStream(VideoCreationCommand command) {
        def temp = temporaryFileService.writeInputStreamToTempFile(command.videoStream, maxVideoStreamSizeInBytes)
        command.videoStreamSizeValidity = (temp != null)

        if(temp) {
            extractStreamsFromVideo(command, temp)
            reloadVideoStreamFromTempFile(command, temp)
            temporaryFileService.deleteTempFile(temp)
        }
    }

    private void extractStreamsFromVideo(VideoCreationCommand command, File temp) {
        def streams = streamMetadataService.extractStreams(temp)
        command.h264StreamIsPresent = streamIsPresent(streams, 'h264')
        command.aacStreamIsPresent = streamIsPresent(streams, 'aac')
        command.durationInSeconds = getLongestStreamDuration(streams)
    }

    private boolean streamIsPresent(List<StreamMetadata> streams, String codec) {
        streams.find { it.codecName == codec } != null
    }

    private Integer getLongestStreamDuration(List<StreamMetadata> streams) {
        def longestStream = streams.max { it.durationInSeconds }
        longestStream?.durationInSeconds
    }

    private void reloadVideoStreamFromTempFile(VideoCreationCommand command, File temp) {
        command.videoStream = new FileInputStream(temp)
    }

    private void validateThumbnailStream(VideoCreationCommand command) {
        def temp = temporaryFileService.writeInputStreamToTempFile(command.thumbnailStream, maxThumbnailStreamSizeInBytes)
        command.thumbnailStreamSizeValidity = (temp != null)

        if(temp) {
            command.thumbnailStream = new BufferedInputStream(new FileInputStream(temp))
            command.thumbnailStream.mark(maxThumbnailStreamSizeInBytes)

            def result = thumbnailValidationService.validateThumbnailStream(command.thumbnailStream)
            command.thumbnailFormatIsValid = result.validFormat

            command.thumbnailStream.reset()
            temporaryFileService.deleteTempFile(temp)
        }
    }

    Video createVideo(VideoCreationCommand command) {

        def creator = command.creator
        def title = command.title

        def videoStream = command.videoStream
        def thumbnailStream = command.thumbnailStream

        def masterThumbnailPath = thumbnailStorageService.uniqueThumbnailPath
        thumbnailStorageService.store(thumbnailStream, masterThumbnailPath)

        def masterPath = videoStorageService.uniqueVideoPath
        videoStorageService.store(videoStream, masterPath)

        def reel = creator.getReel(command.reel)
        def video = new Video(creator: creator, title: title,
                masterPath: masterPath, masterThumbnailPath: masterThumbnailPath)

        reelVideoManagementService.addVideoToReel(reel, video)

        log.info("Created video with id [${video.id}] for user [${creator.username}]")
        def playlistPath = playlistAndSegmentStorageService.uniquePlaylistPath
        transcoderService.transcode(video, playlistPath)

        videoService.storeVideo(video)
        return video
    }

    void completeVideoCreation(String transcoderJobId, String keyPrefix, String variantPlaylistKey) {
        log.debug "Adding playlists from transcoder job [$transcoderJobId] with keyPrefix [$keyPrefix] and variantPlaylistKey [$variantPlaylistKey]"

        def transcoderJob = transcoderJobService.loadJob(transcoderJobId)
        transcoderJobService.complete(transcoderJob)

        def video = transcoderJob.video

        playlistService.addPlaylists(video, keyPrefix, variantPlaylistKey)
        thumbnailService.addThumbnails(video)
    }
}
