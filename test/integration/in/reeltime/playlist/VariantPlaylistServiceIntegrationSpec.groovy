package in.reeltime.playlist

import grails.plugin.spock.IntegrationSpec
import in.reeltime.video.Video

class VariantPlaylistServiceIntegrationSpec extends IntegrationSpec {

    VariantPlaylistService service

    void setup() {
        service = new VariantPlaylistService()
    }

    void "generate variant playlist for video with only one stream"() {
        given:
        def playlist = new Playlist(
                programId: 1,
                bandwidth: 474000,
                resolution: '400x170',
                codecs: 'avc1.42001e,mp4a.40.2'
        )

        def video = new Video(title: 'none', masterPath: 'ignore')
        video.addToPlaylists(playlist)
        video.save()

        assert playlist.id != null

        when:
        def output = service.generateVariantPlaylist(video)

        then:
        output == """#EXTM3U
                    |#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x170,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=474000
                    |${playlist.id}""".stripMargin()
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
        def video = new Video(title: 'none', masterPath: 'ignore')
        video.addToPlaylists(playlist1)
        video.addToPlaylists(playlist2)
        video.save()

        and:
        assert playlist1.id != null
        assert playlist2.id != null

        and:
        def stream1 = """#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=400x170,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=474000
                                |${playlist1.id}""".stripMargin()

        def stream2 = """#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=440x200,CODECS="avc1.42001e,mp4a.40.2",BANDWIDTH=663000
                                |${playlist2.id}""".stripMargin()

        when:
        def output = service.generateVariantPlaylist(video)

        then:
        output.startsWith('#EXTM3U')
        output.contains(stream1)
        output.contains(stream2)
    }
}
