package in.reeltime.playlist

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.video.Video
import spock.lang.Specification

@TestFor(SegmentController)
@Mock([Video, Playlist, Segment, PlaylistVideo, PlaylistSegment])
class SegmentControllerSpec extends Specification {

    PlaylistAndSegmentStorageService playlistAndSegmentStorageService

    void setup() {
        playlistAndSegmentStorageService = Mock(PlaylistAndSegmentStorageService)
        controller.playlistAndSegmentStorageService = playlistAndSegmentStorageService
    }

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
        def segment = new Segment(segmentId: 7819).save(validate: false)
        def playlist = new Playlist().save()

        and:
        def video1 = new Video(title: 'has playlist', available: true).save(validate: false)

        new PlaylistVideo(playlist: playlist, video: video1).save()

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
        def segment = new Segment(uri: 'something.ts', segmentId: 7819).save(validate: false)
        def playlist = new Playlist().save()

        new PlaylistSegment(playlist: playlist, segment: segment).save()

        and:
        def video = new Video(title: 'has playlist', available: true).save(validate: false)

        new PlaylistVideo(playlist: playlist, video: video).save()

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

        when:
        controller.getSegment()

        then:
        1 * playlistAndSegmentStorageService.load(segment.uri) >> segmentStream

        and:
        response.status == 200
        response.contentType == 'video/MP2T'
        response.contentAsString == 'media segment'
    }

    void "multiple segments exist with the same segmentId but for different videos"() {
        given:
        def segment1 = new Segment(uri: 'seg1.ts', segmentId: 0).save(validate: false)
        def segment2 = new Segment(uri: 'seg2.ts', segmentId: 0).save(validate: false)

        def playlist1 = new Playlist().save()
        def playlist2 = new Playlist().save()

        new PlaylistSegment(playlist: playlist1, segment: segment1).save()
        new PlaylistSegment(playlist: playlist2, segment: segment2).save()

        and:
        def video1 = new Video(title: 'video 1', available: true).save(validate: false)
        def video2 = new Video(title: 'video 2', available: true).save(validate: false)

        new PlaylistVideo(playlist: playlist1, video: video1).save()
        new PlaylistVideo(playlist: playlist2, video: video2).save()

        and:
        assert segment1.id
        assert segment2.id

        assert segment1.id != segment2.id
        assert segment1.segmentId == segment2.segmentId

        assert playlist1.id
        assert playlist2.id

        assert video1.id
        assert video2.id

        and:
        params.video_id = video2.id
        params.playlist_id = playlist2.id
        params.segment_id = segment2.segmentId

        and:
        def segmentStream = new ByteArrayInputStream('media segment'.bytes)

        when:
        controller.getSegment()

        then:
        1 * playlistAndSegmentStorageService.load(segment2.uri) >> segmentStream

        and:
        response.status == 200
        response.contentType == 'video/MP2T'
        response.contentAsString == 'media segment'
    }

    void "return a 200 and the requested segment data when playlist has multiple segments"() {
        given:
        def segment1 = new Segment(uri: 'something.ts', segmentId: 7819).save(validate: false)
        def segment2 = new Segment(uri: 'something.ts', segmentId: 1492).save(validate: false)

        def playlist = new Playlist().save()

        new PlaylistSegment(playlist: playlist, segment: segment1).save()
        new PlaylistSegment(playlist: playlist, segment: segment2).save()

        and:
        def video = new Video(title: 'has playlist', available: true).save(validate: false)

        new PlaylistVideo(playlist: playlist, video: video).save()

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
        def segmentStream = new ByteArrayInputStream('media segment'.bytes)

        when:
        controller.getSegment()

        then:
        1 * playlistAndSegmentStorageService.load(segment1.uri) >> segmentStream

        and:
        response.status == 200
        response.contentType == 'video/MP2T'
        response.contentAsString == 'media segment'
    }
}
