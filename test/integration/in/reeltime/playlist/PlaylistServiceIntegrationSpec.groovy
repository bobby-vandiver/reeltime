package in.reeltime.playlist

import grails.test.spock.IntegrationSpec
import in.reeltime.reel.Reel
import in.reeltime.user.User
import in.reeltime.video.Video
import spock.lang.Unroll
import test.helper.UserFactory

class PlaylistServiceIntegrationSpec extends IntegrationSpec {

    def playlistService

    User creator

    void setup() {
        creator = UserFactory.createTestUser()
    }

    void "generate variant playlist for video with only one stream"() {
        given:
        def playlist = new Playlist(
                programId: 1,
                bandwidth: 474000,
                resolution: '400x170',
                codecs: 'avc1.42001e,mp4a.40.2'
        )

        def video = new Video(creator: creator, title: 'none', masterPath: 'ignore')
        video.addToPlaylists(playlist)
        video.save()

        assert playlist.id != null

        when:
        def output = playlistService.generateVariantPlaylist(video)

        then:
        output == """#EXTM3U
                    |#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x170,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=474000
                    |${video.id}/${playlist.id}
                    |""".stripMargin()
    }

    void "generate variant playlist for video with multiple streams"() {
        given:
        def playlist1 = new Playlist(
                programId: 1,
                bandwidth: 474000,
                resolution: '400x170',
                codecs: 'avc1.42001e,mp4a.40.2'
        )

        def playlist2 = new Playlist(
                programId: 1,
                bandwidth: 663000,
                resolution: '440x200',
                codecs: 'avc1.42001e,mp4a.40.2'
        )

        and:
        def video = new Video(creator: creator, title: 'none', masterPath: 'ignore')
        video.addToPlaylists(playlist1)
        video.addToPlaylists(playlist2)
        video.save()

        and:
        assert playlist1.id != null
        assert playlist2.id != null

        and:
        def stream1 = """#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x170,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=474000
                        |${video.id}/${playlist1.id}""".stripMargin()

        def stream2 = """#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=440x200,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=663000
                        |${video.id}/${playlist2.id}""".stripMargin()

        when:
        def output = playlistService.generateVariantPlaylist(video)

        then:
        output.startsWith('#EXTM3U')
        output.contains(stream1)
        output.contains(stream2)
    }

    @Unroll
    void "generate media playlist and allow caching [#allowCacheText]"() {
        given:
        def segment1 = new Segment(segmentId: 0, uri: 'hls-spidey00000.ts', duration: '11.308056')
        def segment2 = new Segment(segmentId: 1, uri: 'hls-spidey00001.ts', duration: '11.262022')

        and:
        def playlist = new Playlist(hlsVersion: 3, mediaSequence: 0, targetDuration: 12)
        playlist.addToSegments(segment1)
        playlist.addToSegments(segment2)

        and:
        def video = new Video(creator: creator, title: 'none', masterPath: 'ignore')
        video.addToPlaylists(playlist)
        video.save()

        and:
        assert segment1.id != null
        assert segment2.id != null

        and:
        def header = """#EXTM3U
                       |#EXT-X-VERSION:3
                       |#EXT-X-MEDIA-SEQUENCE:0
                       |#EXT-X-ALLOW-CACHE:${allowCacheText}
                       |#EXT-X-TARGETDURATION:12""".stripMargin()

        def media1 = """#EXTINF:11.308056,
                       |${playlist.id}/${segment1.segmentId}""".stripMargin()

        def media2 = """#EXTINF:11.262022,
                       |${playlist.id}/${segment2.segmentId}""".stripMargin()

        when:
        def output = playlistService.generateMediaPlaylist(playlist, allowCacheTruth)

        then:
        output.startsWith(header)
        output.contains(media1)
        output.contains(media2)

        where:
        allowCacheTruth     |   allowCacheText
        true                |   'YES'
        false               |   'NO'
    }
}
