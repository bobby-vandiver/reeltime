package in.reeltime.thumbnail

import grails.transaction.Transactional
import in.reeltime.exceptions.ThumbnailGenerationException
import org.apache.commons.io.IOUtils
import org.imgscalr.Scalr

import javax.imageio.ImageIO

import static org.imgscalr.Scalr.Method.QUALITY
import static org.imgscalr.Scalr.Mode.FIT_EXACT

@Transactional
class ThumbnailGenerationService {

    def thumbnailStorageService

    String generateThumbnail(String imagePath, ThumbnailResolution resolution) {
        def imageInputStream = null

        def thumbnailOutputStream = null
        def thumbnailInputStream = null

        try {
            imageInputStream = thumbnailStorageService.load(imagePath)

            def image = ImageIO.read(imageInputStream)
            def thumbnailImage = Scalr.resize(image, QUALITY, FIT_EXACT, resolution.width, resolution.height)

            def temp = File.createTempFile('thumbnail', '.png')

            thumbnailOutputStream = new FileOutputStream(temp)
            ImageIO.write(thumbnailImage, 'png', thumbnailOutputStream)

            thumbnailInputStream = new FileInputStream(temp)
            def path = imagePath + '-' + resolution.name()

            thumbnailStorageService.store(thumbnailInputStream, path)
            return path
        }
        catch(IOException e) {
            def message = "Error occurred while generating thumbnail at resolution [${resolution.name()}] for image [$imagePath]"
            throw new ThumbnailGenerationException(message, e)
        }
        finally {
            IOUtils.closeQuietly(imageInputStream)
            IOUtils.closeQuietly(thumbnailOutputStream)
            IOUtils.closeQuietly(thumbnailInputStream)
        }
    }
}
