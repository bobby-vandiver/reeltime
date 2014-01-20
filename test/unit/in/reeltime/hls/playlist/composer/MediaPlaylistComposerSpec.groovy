package in.reeltime.hls.playlist.composer

import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.MediaSegment
import spock.lang.Specification
import spock.lang.Unroll

class MediaPlaylistComposerSpec extends Specification {

    void "playlist contains no segments"() {

        def playlist = new MediaPlaylist(
                targetDuration: 12,
                mediaSequence: 0,
                version: 3,
                allowCache: true
        )

        def writer = new StringWriter()

        when:
        MediaPlaylistComposer.compose(playlist, writer)

        then:
        true
        writer.toString() == """#EXTM3U
                               |#EXT-X-VERSION:3
                               |#EXT-X-MEDIA-SEQUENCE:0
                               |#EXT-X-ALLOW-CACHE:YES
                               |#EXT-X-TARGETDURATION:12
                               |#EXT-X-ENDLIST""".stripMargin()
    }

    @Unroll
    void "compose media playlist and allowCache [#allowCacheText]"() {
        given:
        def segments = [
                new MediaSegment(uri: 'hls-bats-400k00000.ts', duration: '11.308056'),
                new MediaSegment(uri: 'hls-bats-400k00001.ts', duration: '11.262044')
        ]

        def playlist = new MediaPlaylist(
                targetDuration: 12,
                mediaSequence: 0,
                version: 3,
                allowCache: allowCacheTruth,
                segments: segments
        )

        def writer = new StringWriter()

        when:
        MediaPlaylistComposer.compose(playlist, writer)

        then:
        true
        writer.toString() == """#EXTM3U
                               |#EXT-X-VERSION:3
                               |#EXT-X-MEDIA-SEQUENCE:0
                               |#EXT-X-ALLOW-CACHE:${allowCacheText}
                               |#EXT-X-TARGETDURATION:12
                               |#EXTINF:11.308056,
                               |hls-bats-400k00000.ts
                               |#EXTINF:11.262044,
                               |hls-bats-400k00001.ts
                               |#EXT-X-ENDLIST""".stripMargin()

        where:
        allowCacheTruth     |   allowCacheText
        true                |   'YES'
        false               |   'NO'
    }
}
