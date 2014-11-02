package in.reeltime.playlist

import grails.plugin.springsecurity.annotation.Secured
import in.reeltime.video.Video
import static javax.servlet.http.HttpServletResponse.*

class SegmentController {

    def outputStorageService

    static allowedMethods = [getSegment: 'GET']

    @Secured(["#oauth2.hasScope('videos-read')"])
    def getSegment(long video_id, long playlist_id, int segment_id) {

        log.debug("Requested segment [${segment_id}] for playlist [${playlist_id}] belonging to video [${video_id}]")

        def video = Video.findByIdAndAvailable(video_id, true)
        def playlist = Playlist.findByIdAndVideo(playlist_id, video)

        def segment = Segment.findBySegmentIdAndPlaylist(segment_id, playlist)

        if(segment) {
            def stream = outputStorageService.load(segment.uri) as InputStream
            def content = stream.bytes

            response.status = SC_OK
            response.contentType = 'video/MP2T'
            response.contentLength = content.size()
            response.outputStream << content
        }
        else {
            render status: SC_NOT_FOUND
        }
    }
}
