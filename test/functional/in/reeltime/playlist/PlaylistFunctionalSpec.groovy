package in.reeltime.playlist

import in.reeltime.FunctionalSpec
import in.reeltime.hls.playlist.MediaSegment
import in.reeltime.hls.playlist.StreamAttributes
import spock.lang.Unroll

class PlaylistFunctionalSpec extends FunctionalSpec {

    String token

    void setup() {
        token = registerNewUserAndGetToken('playlist', ['videos-read', 'videos-write'])
    }

    void "invalid http methods"() {
        given:
        def variantPlaylistUrl = urlFactory.getVariantPlaylistUrl(1234)
        def mediaPlaylistUrl = urlFactory.getMediaPlaylistUrl(1234, 5678)
        def segmentUrl = urlFactory.getSegmentUrl(1234, 5678, 90)

        and:
        def invalidMethods = ['post', 'put', 'delete']

        expect:
        responseChecker.assertInvalidHttpMethods(variantPlaylistUrl, invalidMethods)
        responseChecker.assertInvalidHttpMethods(mediaPlaylistUrl, invalidMethods)
        responseChecker.assertInvalidHttpMethods(segmentUrl, invalidMethods)
    }

    void "request invalid variant playlist"() {
        given:
        def request = requestFactory.variantPlaylist(token, -1)

        when:
        def response = get(request)

        then:
        responseChecker.assertStatusCode(response, 404)
    }

    @Unroll
    void "request invalid media playlist -- video_id [#videoId], playlist_id [#playlistId]"() {
        given:
        def request = requestFactory.mediaPlaylist(token, videoId, playlistId)

        when:
        def response = get(request)

        then:
        responseChecker.assertStatusCode(response, 404)

        where:
        videoId     |   playlistId
        -1          |   1234
        1234        |   -1
    }

    @Unroll
    void "request invalid segment -- video_id [#videoId], playlist_id [#playlistId], segment_id [#segmentId]"() {
        given:
        def request = requestFactory.mediaPlaylist(token, videoId, playlistId)

        when:
        def response = get(request)

        then:
        responseChecker.assertStatusCode(response, 404)

        where:
        videoId     |   playlistId  |   segmentId
        -1          |   1234        |   5678
        1234        |   -1          |   5678
        1234        |   5678        |   -1
    }

    void "upload and stream video"() {
        given:
        def videoId = reelTimeClient.uploadVideoToUncategorizedReel(token)

        when:
        def variantPlaylist = reelTimeClient.variantPlaylist(token, videoId)

        then:
        variantPlaylist.streams.size() > 0

        for(StreamAttributes stream in variantPlaylist.streams) {
            def playlistId = extractMediaPlaylistIdFromUri(stream.uri)
            def mediaPlaylist = reelTimeClient.mediaPlaylist(token, videoId, playlistId)

            assert mediaPlaylist.segments.size() > 0

            for(MediaSegment segment in mediaPlaylist.segments) {
                def segmentId = extractSegmentIdFromUri(segment.uri)
                assert reelTimeClient.mediaSegment(token, videoId, playlistId, segmentId)
            }
        }
    }

    private Long extractMediaPlaylistIdFromUri(String uri) {
        def tokens = uri.tokenize('-')
        def playlist = tokens[1]

        def id = playlist.replace('.m3u8', '')
        Long.parseLong(id)
    }

    private Long extractSegmentIdFromUri(String uri) {
        def tokens = uri.tokenize('-')
        def segment = tokens[2]

        def id = segment.replace('.ts', '')
        Long.parseLong(id)
    }
}
