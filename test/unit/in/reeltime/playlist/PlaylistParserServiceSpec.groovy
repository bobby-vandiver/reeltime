package in.reeltime.playlist

import grails.test.mixin.TestFor
import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.VariantPlaylist
import in.reeltime.storage.PlaylistAndSegmentStorageService
import spock.lang.Specification

@TestFor(PlaylistParserService)
class PlaylistParserServiceSpec extends Specification {

    PlaylistAndSegmentStorageService playlistAndSegmentStorageService

    void setup() {
        playlistAndSegmentStorageService = Mock(PlaylistAndSegmentStorageService)
        service.playlistAndSegmentStorageService = playlistAndSegmentStorageService

        service.maxRetries = 5
        service.intervalInMillis = 500
    }

    void "load variant playlist from storage and parse it"() {
        given:
        def text = '''#EXTM3U
                     |#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x226,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=455000
                     |hls-bats-400k.m3u8
                     |#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=480x270,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=663000
                     |hls-bats-600k.m3u8'''.stripMargin()

        def playlistStream = new ByteArrayInputStream(text.bytes)

        and:
        def path = 'holy-streaming-video-batman/hls-bats-master.m3u8'

        when:
        def variantPlaylist = service.parseVariantPlaylist(path) as VariantPlaylist

        then:
        1 * playlistAndSegmentStorageService.load(path) >> playlistStream
        3 * playlistAndSegmentStorageService.exists(path) >>> [false, true]

        and:
        variantPlaylist.streams.size() == 2

        and:
        def stream1 = variantPlaylist.streams[0]
        def stream2 = variantPlaylist.streams[1]

        and:
        stream1.uri == 'hls-bats-400k.m3u8'
        stream1.programId == 1
        stream1.resolution == '400x226'
        stream1.codecs == 'avc1.42001e,mp4a.40.2'
        stream1.bandwidth == 455000

        and:
        stream2.uri == 'hls-bats-600k.m3u8'
        stream2.programId == 1
        stream2.resolution == '480x270'
        stream2.codecs == 'avc1.42001e,mp4a.40.2'
        stream2.bandwidth == 663000
    }

    void "load media playlist from storage and parse it"() {
        given:
        def text = '''#EXTM3U
                     |#EXT-X-VERSION:3
                     |#EXT-X-MEDIA-SEQUENCE:0
                     |#EXT-X-ALLOW-CACHE:YES
                     |#EXT-X-TARGETDURATION:12
                     |#EXTINF:11.308056,
                     |hls-bats-400k00000.ts
                     |#EXTINF:11.262044,
                     |hls-bats-400k00001.ts
                     |#EXTINF:7.524100,
                     |hls-bats-400k00002.ts
                     |#EXTINF:11.278267,
                     |hls-bats-400k00003.ts
                     |#EXTINF:11.278689,
                     |hls-bats-400k00004.ts
                     |#EXTINF:7.517500,
                     |hls-bats-400k00005.ts
                     |#EXTINF:11.271656,
                     |hls-bats-400k00006.ts
                     |#EXTINF:11.272111,
                     |hls-bats-400k00007.ts
                     |#EXTINF:7.510911,
                     |hls-bats-400k00008.ts
                     |#EXTINF:0.792889,
                     |hls-bats-400k00009.ts
                     |#EXT-X-ENDLIST'''.stripMargin()

        def playlistStream = new ByteArrayInputStream(text.bytes)

        and:
        def path = 'holy-streaming-video-batman/hls-bats-400k.m3u8'

        when:
        def mediaPlaylist = service.parseMediaPlaylist(path) as MediaPlaylist

        then:
        1 * playlistAndSegmentStorageService.load(path) >> playlistStream
        3 * playlistAndSegmentStorageService.exists(path) >>> [false, true]

        and:
        mediaPlaylist.version == 3
        mediaPlaylist.mediaSequence == 0
        mediaPlaylist.targetDuration == 12
        mediaPlaylist.allowCache

        and:
        def segments = mediaPlaylist.segments
        segments.size() == 10

        and:
        segments[0].uri == 'hls-bats-400k00000.ts'
        segments[0].duration == '11.308056'

        and:
        segments[1].uri == 'hls-bats-400k00001.ts'
        segments[1].duration == '11.262044'

        and:
        segments[2].uri == 'hls-bats-400k00002.ts'
        segments[2].duration == '7.524100'

        and:
        segments[3].uri == 'hls-bats-400k00003.ts'
        segments[3].duration == '11.278267'

        and:
        segments[4].uri == 'hls-bats-400k00004.ts'
        segments[4].duration == '11.278689'

        and:
        segments[5].uri == 'hls-bats-400k00005.ts'
        segments[5].duration == '7.517500'

        and:
        segments[6].uri == 'hls-bats-400k00006.ts'
        segments[6].duration == '11.271656'

        and:
        segments[7].uri == 'hls-bats-400k00007.ts'
        segments[7].duration == '11.272111'

        and:
        segments[8].uri == 'hls-bats-400k00008.ts'
        segments[8].duration == '7.510911'

        and:
        segments[9].uri == 'hls-bats-400k00009.ts'
        segments[9].duration == '0.792889'
    }

    void "exceed max number of retries when waiting for playlist to become available"() {
        when:
        service.waitUntilPlaylistIsAvailable('somewhere')

        then:
        def e = thrown(IllegalArgumentException)
        e.message == '[somewhere] does not exist!'

        and:
        playlistAndSegmentStorageService.exists('somewhere') >> false
    }
}
