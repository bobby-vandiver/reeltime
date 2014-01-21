package in.reeltime.playlist

import grails.plugin.spock.IntegrationSpec
import in.reeltime.video.Video
import spock.lang.Unroll

class MediaPlaylistServiceIntegrationSpec extends IntegrationSpec {

    MediaPlaylistService service

    void setup() {
        service = new MediaPlaylistService()
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
        def video = new Video(title: 'none', masterPath: 'ignore')
        video.addToPlaylists(playlist)
        video.save()

        and:
        segment1.id != null
        segment2.id != null

        and:
        def header = """#EXTM3U
                       |#EXT-X-VERSION:3
                       |#EXT-X-MEDIA-SEQUENCE:0
                       |#EXT-X-ALLOW-CACHE:${allowCacheText}
                       |#EXT-X-TARGETDURATION:12""".stripMargin()

        def media1 = """#EXTINF:11.308056,
                       |${segment1.id}""".stripMargin()

        def media2 = """#EXTINF:11.262022,
                       |${segment2.id}""".stripMargin()

        when:
        def output = service.generateMediaPlaylist(playlist, allowCacheTruth)

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
