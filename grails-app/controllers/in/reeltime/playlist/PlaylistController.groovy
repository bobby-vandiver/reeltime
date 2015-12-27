package in.reeltime.playlist

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.video.Video
import in.reeltime.video.VideoCommand

import static javax.servlet.http.HttpServletResponse.*

class PlaylistController extends AbstractController {

    def playlistService

    @Secured(["#oauth2.hasScope('videos-read')"])
    def getVariantPlaylist(VideoCommand command) {
        log.debug("Requested variant playlist for video [${command.video_id}]")

        handleCommandRequest(command) {
            def video = Video.findByIdAndAvailable(command.video_id, true)
            if (video) {
                response.status = SC_OK
                response.contentType = 'application/x-mpegURL'
                response.outputStream << playlistService.generateVariantPlaylist(video)
            } else {
                render status: SC_NOT_FOUND
            }
        }
    }

    @Secured(["#oauth2.hasScope('videos-read')"])
    def getMediaPlaylist(VideoCommand videoCommand, PlaylistCommand playlistCommand) {
        log.debug("Requested media playlist [${playlistCommand.playlist_id}] for video [${videoCommand.video_id}]")

        handleMultipleCommandRequest([videoCommand, playlistCommand]) {
            def video = Video.findByIdAndAvailable(videoCommand.video_id, true)
            
            def playlist = Playlist.findById(playlistCommand.playlist_id)
            def playlistBelongsToVideo = playlist && PlaylistVideo.findByPlaylistAndVideo(playlist, video)

            if (playlistBelongsToVideo) {
                response.status = SC_OK
                response.contentType = 'application/x-mpegURL'
                response.outputStream << playlistService.generateMediaPlaylist(playlist, true)
            } else {
                render status: SC_NOT_FOUND
            }
        }
    }
}
