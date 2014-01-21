package in.reeltime.playlist

import in.reeltime.video.Video
import static javax.servlet.http.HttpServletResponse.*

class VariantPlaylistController {

    def playlistService

    def getVariantPlaylist() {

        def video = Video.findById(params.videoId)

        if(video) {
            response.status = SC_OK
            response.contentType = 'application/x-mpegURL'
            response.outputStream << playlistService.generateVariantPlaylist(video)
        }
        else {
            response.status = SC_NOT_FOUND
        }
    }
}
