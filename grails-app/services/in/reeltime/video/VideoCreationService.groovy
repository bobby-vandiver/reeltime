package in.reeltime.video

import in.reeltime.metadata.StreamMetadata

class VideoCreationService {

    def playlistAndSegmentStorageService
    def videoStorageService

    def transcoderService
    def transcoderJobService

    def streamMetadataService
    def playlistService

    def videoService
    def reelVideoManagementService

    def thumbnailStorageService
    def thumbnailValidationService

    def maxVideoStreamSizeInBytes

    private static final int BUFFER_SIZE = 8 * 1024

    boolean allowCreation(VideoCreationCommand command) {

        def temp = writeVideoStreamToTempFile(command)
        setVideoStreamSizeIsValid(command, temp)

        if(temp) {
            extractStreamsFromVideo(command, temp)
            reloadVideoStreamFromTempFile(command, temp)
            deleteTempFile(temp)
        }

        validateThumbnail(command)
        return command.validate()
    }

    private void validateThumbnail(VideoCreationCommand command) {
        if(command.thumbnailStream) {
            def result = thumbnailValidationService.validateThumbnailStream(command.thumbnailStream)
            command.thumbnailFormatIsValid = result.validFormat
        }
    }

    private File writeVideoStreamToTempFile(VideoCreationCommand command) {
        OutputStream outputStream = null
        try {
            def videoStream = command.videoStream
            if(!videoStream) {
                log.warn("Video stream not available")
                return null
            }

            log.debug("Creating temp file for video stream")
            def temp = File.createTempFile('can-create-video', '.tmp')

            def fos = new FileOutputStream(temp)
            outputStream = new BufferedOutputStream(fos)

            byte[] buffer = new byte[BUFFER_SIZE]

            int totalBytesRead = 0
            int bytesRead

            log.debug("Reading video stream into buffer")
            while((bytesRead = videoStream.read(buffer)) >= 0) {
                totalBytesRead += bytesRead

                if(totalBytesRead > maxVideoStreamSizeInBytes) {
                    log.warn("Video stream exceeds max allowed size")
                    return null
                }

                log.trace("Writing $bytesRead bytes to the buffer")
                outputStream.write(buffer, 0, bytesRead)
            }
            return temp
        }
        catch(IOException e) {
            log.warn("Failed to write video stream to temp file", e)
            return null
        }
        finally {
            if(outputStream) {
                log.debug("Closing output stream for temp file")
                outputStream.close()
            }
        }
    }

    private static void setVideoStreamSizeIsValid(VideoCreationCommand command, File temp) {
        Boolean valid
        if(!command.videoStream) {
            valid = null
        }
        else if(temp) {
            valid = true
        }
        else {
            valid = false
        }
        command.videoStreamSizeIsValid = valid
    }

    private void extractStreamsFromVideo(VideoCreationCommand command, File temp) {
        def streams = streamMetadataService.extractStreams(temp)
        command.h264StreamIsPresent = streamIsPresent(streams, 'h264')
        command.aacStreamIsPresent = streamIsPresent(streams, 'aac')
        command.durationInSeconds = getLongestStreamDuration(streams)
    }

    private static boolean streamIsPresent(List<StreamMetadata> streams, String codec) {
        streams.find { it.codecName == codec } != null
    }

    private static Integer getLongestStreamDuration(List<StreamMetadata> streams) {
        def longestStream = streams.max { it.durationInSeconds }
        longestStream?.durationInSeconds
    }

    private static void reloadVideoStreamFromTempFile(VideoCreationCommand command, File temp) {
        command.videoStream = new FileInputStream(temp)
    }

    private static void deleteTempFile(File temp) {
        if(!temp.delete()) {
            temp.deleteOnExit()
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
        def outputPath = playlistAndSegmentStorageService.uniquePlaylistPath
        transcoderService.transcode(video, outputPath)

        videoService.storeVideo(video)
        return video
    }

    void completeVideoCreation(String transcoderJobId, String keyPrefix, String variantPlaylistKey) {
        log.debug "Adding playlists from transcoder job [$transcoderJobId] with keyPrefix [$keyPrefix] and variantPlaylistKey [$variantPlaylistKey]"

        def transcoderJob = transcoderJobService.loadJob(transcoderJobId)
        transcoderJobService.complete(transcoderJob)

        def video = transcoderJob.video
        playlistService.addPlaylists(video, keyPrefix, variantPlaylistKey)
    }
}
