package in.reeltime.thumbnail

import in.reeltime.common.AbstractController
import in.reeltime.video.VideoCommand
import static javax.servlet.http.HttpServletResponse.*

class ThumbnailController extends AbstractController {

    def thumbnailService
    def thumbnailStorageService

    def getThumbnail(VideoCommand videoCommand, ThumbnailCommand thumbnailCommand) {
        log.debug("Requested thumbnail [${thumbnailCommand.resolution} for video [${videoCommand.video_id}]")

        handleMultipleCommandRequest([videoCommand, thumbnailCommand]) {
            def thumbnail = thumbnailService.loadThumbnail(videoCommand.video_id, thumbnailCommand.thumbnailResolution)

            response.status = SC_OK
            response.contentType = 'image/png'
            response.outputStream << thumbnailStorageService.load(thumbnail.uri)
        }
    }
}
