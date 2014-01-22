package in.reeltime.playlist

import in.reeltime.video.Video
import static javax.servlet.http.HttpServletResponse.*

class SegmentController {

    def outputStorageService

    static allowedMethods = [getSegment: 'GET']

    def getSegment() {

        log.debug("Requested segment [${params.segmentId}] for playlist [${params.playlistId}] belonging to video [${params.videoId}]")

        def video = Video.findById(params.videoId)
        def playlist = Playlist.findByIdAndVideo(params.playlistId, video)

        def segment = Segment.findBySegmentIdAndPlaylist(params.segmentId, playlist)

        if(segment) {
            def stream = outputStorageService.load(segment.uri) as InputStream
            def content = stream.bytes

            response.status = SC_OK
            response.contentType = 'video/MP2T'
            response.contentLength = content.size()
            response.outputStream << content
        }
        else {
            response.status = SC_NOT_FOUND
        }
    }
}
