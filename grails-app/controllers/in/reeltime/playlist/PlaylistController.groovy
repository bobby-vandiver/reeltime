package in.reeltime.playlist

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.video.Video
import static javax.servlet.http.HttpServletResponse.*

class PlaylistController {

    def playlistService

    static allowedMethods = [getVariantPlaylist: 'GET', getMediaPlaylist: 'GET']

    @Secured(["#oauth2.hasScope('videos-read')"])
    def getVariantPlaylist(long video_id) {

        log.debug("Requested variant playlist for video [${video_id}]")
        def video = Video.findByIdAndAvailable(video_id, true)

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
    def getMediaPlaylist(long video_id, long playlist_id) {

        log.debug("Requested media playlist [${playlist_id}] for video [${video_id}]")

        def video = Video.findByIdAndAvailable(video_id, true)
        def playlist = Playlist.findByIdAndVideo(playlist_id, video)

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
