package in.reeltime.storage

class TemporaryFileService {

    private static final int BUFFER_SIZE = 8 * 1024

    File writeInputStreamToTempFile(InputStream inputStream, int maxStreamSizeInBytes) {
        OutputStream outputStream = null
        try {
            if(!inputStream) {
                log.warn("Input stream not available")
                return null
            }

            log.debug("Creating temp file for stream")
            def temp = File.createTempFile('input-stream', '.tmp')

            def fos = new FileOutputStream(temp)
            outputStream = new BufferedOutputStream(fos)

            byte[] buffer = new byte[BUFFER_SIZE]

            int totalBytesRead = 0
            int bytesRead

            log.debug("Reading stream into buffer")
            while((bytesRead = inputStream.read(buffer)) >= 0) {
                totalBytesRead += bytesRead

                if(totalBytesRead > maxStreamSizeInBytes) {
                    log.warn("Stream exceeds max allowed size")
                    return null
                }

                log.trace("Writing $bytesRead bytes to the buffer")
                outputStream.write(buffer, 0, bytesRead)
            }
            return temp
        }
        catch(IOException e) {
            log.warn("Failed to write stream to temp file", e)
            return null
        }
        finally {
            if(outputStream) {
                log.debug("Closing output stream for temp file")
                outputStream.close()
            }
        }
    }

    void deleteTempFile(File temp) {
        if(!temp.delete()) {
            temp.deleteOnExit()
        }
    }
}
