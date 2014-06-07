package in.reeltime.video

import in.reeltime.user.User

class VideoCreationService {

    def pathGenerationService
    def inputStorageService

    def transcoderService
    def streamMetadataService

    def maxVideoStreamSizeInBytes

    private static final int BUFFER_SIZE = 8 * 1024

    boolean allowCreation(VideoCreationCommand command) {

        def temp = writeVideoStreamToTempFile(command)
        if(temp) {
            extractStreamsFromVideo(command, temp)
            reloadVideoStreamFromTempFile(command, temp)
        }
        return command.validate()
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

    private void extractStreamsFromVideo(VideoCreationCommand command, File temp) {
        command.streams = streamMetadataService.extractStreams(temp)
    }

    private static void reloadVideoStreamFromTempFile(VideoCreationCommand command, File temp) {
        command.videoStream = new FileInputStream(temp)
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
