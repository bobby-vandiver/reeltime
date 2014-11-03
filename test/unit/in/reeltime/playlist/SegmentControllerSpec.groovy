package in.reeltime.playlist

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.video.Video
import spock.lang.Specification
import in.reeltime.storage.OutputStorageService

@TestFor(SegmentController)
@Mock([Video, Playlist, Segment])
class SegmentControllerSpec extends Specification {

    void "return a 404 if the video exists but is not available"() {
        given:
        def video = new Video(title: 'test', available: false).save(validate: false)
        params.video_id = video.id
        assert video.id

        params.playlist_id = 1234
        params.segment_id = 5678

        when:
        controller.getSegment()

        then:
        response.status == 404
    }

    void "return a 404 if the playlist does not belong to the video"() {
        given:
        def segment = new Segment(segmentId: 7819)

        def playlist = new Playlist()
        playlist.addToSegments(segment)

        and:
        def video1 = new Video(title: 'has playlist', available: true)
        video1.addToPlaylists(playlist)
        video1.save(validate: false)

        and:
        def video2 = new Video(title: 'no playlist', available: true).save(validate: false)

        and:
        assert segment.id
        assert playlist.id
        assert video1.id != video2.id

        and:
        params.video_id = video2.id
        params.playlist_id = playlist.id
        params.segment_id = segment.segmentId

        when:
        controller.getSegment()

        then:
        response.status == 404
    }

    void "return a 200 and the requested segment data when playlist has only one segment"() {
        given:
        def segment = new Segment(uri: 'something.ts', segmentId: 7819)

        def playlist = new Playlist()
        playlist.addToSegments(segment)

        and:
        def video = new Video(title: 'has playlist', available: true)
        video.addToPlaylists(playlist)
        video.save(validate: false)

        and:
        assert playlist.id
        assert video.id
        assert segment.id

        and:
        params.video_id = video.id
        params.playlist_id = playlist.id
        params.segment_id = segment.segmentId

        and:
        def segmentStream = new ByteArrayInputStream('media segment'.bytes)

        and:
        controller.outputStorageService = Mock(OutputStorageService)

        when:
        controller.getSegment()

        then:
        1 * controller.outputStorageService.load(segment.uri) >> segmentStream

        and:
        response.status == 200
        response.contentType == 'video/MP2T'
        response.contentAsString == 'media segment'
    }

    void "return a 200 and the requested segment data when playlist has multiple segments"() {
        given:
        def segment1 = new Segment(uri: 'something.ts', segmentId: 7819)
        def segment2 = new Segment(uri: 'something.ts', segmentId: 1492)

        def playlist = new Playlist()
        playlist.addToSegments(segment1)
        playlist.addToSegments(segment2)

        and:
        def video = new Video(title: 'has playlist', available: true)
        video.addToPlaylists(playlist)
        video.save(validate: false)

        and:
        assert playlist.id
        assert video.id
        assert segment1.id
        assert segment2.id

        and:
        params.video_id = video.id
        params.playlist_id = playlist.id
        params.segment_id = segment1.segmentId

        and:
        def data = new File('test/files/sample.ts')
        def segmentStream = data.newInputStream()

        and:
        controller.outputStorageService = Mock(OutputStorageService)

        when:
        controller.getSegment()

        then:
        1 * controller.outputStorageService.load(segment1.uri) >> segmentStream

        and:
        response.status == 200
        response.contentType == 'video/MP2T'
        response.contentAsByteArray == data.bytes
        response.contentLength == data.bytes.size()
    }
}
