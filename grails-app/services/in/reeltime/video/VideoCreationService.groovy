package in.reeltime.video

import in.reeltime.metadata.StreamMetadata
import in.reeltime.user.User

class VideoCreationService {

    def pathGenerationService
    def inputStorageService

    def transcoderService
    def streamMetadataService

    def grailsApplication

    private static final int BUFFER_SIZE = 8 * 1024

    boolean allowCreation(VideoCreationCommand command) {

        def temp = writeVideoStreamToTempFile(command)
        if(temp) {
            def streams = streamMetadataService.extractStreams(temp)
            reloadVideoStreamFromTempFile(command, temp)
            return validateStreams(streams)
        }
        return false
    }

    private File writeVideoStreamToTempFile(VideoCreationCommand command) {
        def maxSize = grailsApplication.config.reeltime.metadata.maxVideoStreamSizeInBytes as int
        OutputStream outputStream = null
        try {
            log.debug("Creating temp file for video stream")
            def temp = File.createTempFile('can-create-video', '.tmp')

            def fos = new FileOutputStream(temp)
            outputStream = new BufferedOutputStream(fos)

            def videoStream = command.videoStream
            byte[] buffer = new byte[BUFFER_SIZE]

            int totalBytesRead = 0
            int bytesRead

            log.debug("Reading video stream into buffer")
            while((bytesRead = videoStream.read(buffer)) >= 0) {
                totalBytesRead += bytesRead

                if(totalBytesRead > maxSize) {
                    log.warn("Video stream exceeds max allowed size")
                    return null
                }

                log.debug("Writing $bytesRead bytes to the buffer")
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

    private static void reloadVideoStreamFromTempFile(VideoCreationCommand command, File temp) {
        command.videoStream = new FileInputStream(temp)
    }

    private static boolean validateStreams(List<StreamMetadata> streams) {
        !streams.empty && streams.find { !it.validate() } == null
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
