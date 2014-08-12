package in.reeltime.playlist

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.video.Video
import static javax.servlet.http.HttpServletResponse.*

class PlaylistController {

    def playlistService

    static allowedMethods = [getVariantPlaylist: 'GET', getMediaPlaylist: 'GET']

    @Secured(["#oauth2.hasScope('videos-read')"])
    def getVariantPlaylist(long videoId) {

        log.debug("Requested variant playlist for video [${videoId}]")
        def video = Video.findByIdAndAvailable(videoId, true)

        if(video) {
            response.status = SC_OK
            response.contentType = 'application/x-mpegURL'
            response.outputStream << playlistService.generateVariantPlaylist(video)
        }
        else {
            render status: SC_NOT_FOUND
        }
    }

    @Secured(["#oauth2.hasScope('videos-read')"])
    def getMediaPlaylist(long videoId, long playlistId) {

        log.debug("Requested media playlist [${playlistId}] for video [${videoId}]")

        def video = Video.findByIdAndAvailable(videoId, true)
        def playlist = Playlist.findByIdAndVideo(playlistId, video)

        if(playlist) {
            response.status = SC_OK
            response.contentType = 'application/x-mpegURL'
            response.outputStream << playlistService.generateMediaPlaylist(playlist, true)
        }
        else {
            render status: SC_NOT_FOUND
        }
    }
}
