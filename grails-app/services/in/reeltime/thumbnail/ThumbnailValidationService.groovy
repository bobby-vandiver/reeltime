package in.reeltime.thumbnail

import org.apache.tika.Tika

class ThumbnailValidationService {

    ThumbnailValidationResult validateThumbnailStream(InputStream thumbnailStream) {
        boolean valid = false
        Tika tika = new Tika()

        try {
            if(thumbnailStream) {
                def bufferedStream = new BufferedInputStream(thumbnailStream)
                def mimeType = tika.detect(bufferedStream)
                valid = (mimeType == 'image/png')
            }
        }
        catch(IOException e) {
            log.warn("Exception occurred while validating thumbnail stream:", e)
        }

        return new ThumbnailValidationResult(validFormat: valid)
    }
}
