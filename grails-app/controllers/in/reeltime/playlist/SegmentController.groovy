package in.reeltime.playlist

import in.reeltime.video.Video
import static javax.servlet.http.HttpServletResponse.*

class SegmentController {

    def outputStorageService

    def getSegment() {

        def video = Video.findById(params.videoId)
        def playlist = Playlist.findByIdAndVideo(params.playlistId, video)

        def segment = Segment.findBySegmentIdAndPlaylist(params.segmentId, playlist)

        if(segment) {
            response.status = SC_OK
            response.contentType = 'video/MP2T'
            response.outputStream << outputStorageService.load(segment.uri)
        }
        else {
            response.status = SC_NOT_FOUND
        }
    }
}
