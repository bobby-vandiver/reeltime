package in.reeltime.playlist

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.common.AbstractController
import in.reeltime.video.Video
import in.reeltime.video.VideoCommand

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import static javax.servlet.http.HttpServletResponse.SC_OK

class SegmentController extends AbstractController {

    def playlistAndSegmentStorageService

    @Secured(["#oauth2.hasScope('videos-read')"])
    def getSegment(VideoCommand videoCommand, PlaylistCommand playlistCommand, SegmentCommand segmentCommand) {

        def videoId = videoCommand.video_id
        def playlistId = playlistCommand.playlist_id
        def segmentId = segmentCommand.segment_id

        log.debug("Requested segment [${segmentId}] for playlist [${playlistId}] belonging to video [${videoId}]")

        handleMultipleCommandRequest([videoCommand, playlistCommand, segmentCommand]) {
            def video = Video.findByIdAndAvailable(videoId, true)

            def playlist = Playlist.findById(playlistId)
            def playlistBelongsToVideo = PlaylistVideo.findByPlaylistAndVideo(playlist, video)

            def segment = Segment.findBySegmentId(segmentId)
            def segmentBelongsToPlaylist = PlaylistSegment.findByPlaylistAndSegment(playlist, segment)

            if (playlistBelongsToVideo && segmentBelongsToPlaylist) {
                def stream = playlistAndSegmentStorageService.load(segment.uri) as InputStream
                def content = stream.bytes

                response.status = SC_OK
                response.contentType = 'video/MP2T'
                response.contentLength = content.size()
                response.outputStream << content
            } else {
                render status: SC_NOT_FOUND
            }
        }
    }
}
