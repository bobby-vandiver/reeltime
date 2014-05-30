package in.reeltime.playlist

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.video.Video
import static javax.servlet.http.HttpServletResponse.*

class PlaylistController {

    def playlistService

    static allowedMethods = [getVariantPlaylist: 'GET', getMediaPlaylist: 'GET']

    @Secured(["#oauth2.hasScope('view')"])
    def getVariantPlaylist() {

        log.debug("Requested variant playlist for video [${params.videoId}]")
        def video = Video.findById(params.videoId)

        if(video) {
            response.status = SC_OK
            response.contentType = 'application/x-mpegURL'
            response.outputStream << playlistService.generateVariantPlaylist(video)
        }
        else {
            render status: SC_NOT_FOUND
        }
    }

    @Secured(["#oauth2.hasScope('view')"])
    def getMediaPlaylist() {

        log.debug("Requested media playlist [${params.playlistId}] for video [${params.videoId}]")

        def video = Video.findById(params.videoId)
        def playlist = Playlist.findByIdAndVideo(params.playlistId, video)

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
