package in.reeltime.playlist

import in.reeltime.video.Video
import static javax.servlet.http.HttpServletResponse.*

class PlaylistController {

    def playlistService

    static allowedMethods = [getVariantPlaylist: 'GET', getMediaPlaylist: 'GET']

    def getVariantPlaylist() {

        log.debug("Requested variant playlist for video [${params.videoId}")
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

    def getMediaPlaylist() {

        log.debug("Requested media playlist [${params.playlistId}] for video [${params.videoId}")

        def video = Video.findById(params.videoId)
        def playlist = Playlist.findByIdAndVideo(params.playlistId, video)

        if(playlist) {
            response.status = SC_OK
            response.contentType = 'application/x-mpegURL'
            response.outputStream << playlistService.generateMediaPlaylist(playlist, true)
        }
        else {
            response.status = SC_NOT_FOUND
        }
    }
}
