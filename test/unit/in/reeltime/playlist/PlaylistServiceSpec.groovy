package in.reeltime.playlist

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.MediaSegment
import in.reeltime.hls.playlist.StreamAttributes
import in.reeltime.hls.playlist.VariantPlaylist
import in.reeltime.video.Video
import spock.lang.Specification

@TestFor(PlaylistService)
@Mock([Video, Playlist, Segment])
class PlaylistServiceSpec extends Specification {

    void "combine metadata in variant playlist and one media playlist into model for a single stream"() {
        given:
        service.playlistParserService = Mock(PlaylistParserService)

        and:
        def video = new Video(title: 'awesome', masterPath: 'pathToVideo').save()
        def variantPlaylistKey = 'master-playlist'

        and:
        def stream = new StreamAttributes(uri: 'media-low.m3u8', bandwidth: 12, programId: 1, codecs: 'bar', resolution: 'buzz')
        def variantPlaylist = new VariantPlaylist(streams: [stream])

        and:
        def segments = [
                new MediaSegment(uri: 'seg0.ts', duration: '12.40'),
                new MediaSegment(uri: 'seg1.ts', duration: '2.3111')
        ]

        def mediaPlaylist = new MediaPlaylist(targetDuration: 10, mediaSequence: 20,
                version: 3, allowCache: true, segments: segments)

        and:
        def keyPrefix = 'base/'

        when:
        service.addPlaylists(video, keyPrefix, variantPlaylistKey)

        then:
        1 * service.playlistParserService.parseVariantPlaylist(keyPrefix + variantPlaylistKey + '.m3u8') >> variantPlaylist
        1 * service.playlistParserService.parseMediaPlaylist(keyPrefix + stream.uri) >> mediaPlaylist

        and:
        video.available
        video.playlists.size() == 1

        and:
        def playlist = (video.playlists as List)[0]
        playlist.video == video
        playlist.id != null

        and:
        playlist.codecs == stream.codecs
        playlist.bandwidth == stream.bandwidth
        playlist.programId == stream.programId
        playlist.resolution == stream.resolution

        and:
        playlist.hlsVersion == mediaPlaylist.version
        playlist.targetDuration == mediaPlaylist.targetDuration
        playlist.mediaSequence == mediaPlaylist.mediaSequence

        and:
        playlist.segments.size() == 2

        and:
        def segment1 = (playlist.segments as List)[0] as Segment
        segment1.segmentId == 0
        segment1.uri == keyPrefix + segments[0].uri
        segment1.duration == segments[0].duration

        and:
        def segment2 = (playlist.segments as List)[1] as Segment
        segment2.segmentId == 1
        segment2.uri == keyPrefix + segments[1].uri
        segment2.duration == segments[1].duration
    }

    void "playlist has more than one stream variant"() {
        given:
        service.playlistParserService = Mock(PlaylistParserService)

        and:
        def video = new Video(title: 'awesome', masterPath: 'pathToVideo').save()

        and:
        def stream1 = new StreamAttributes(uri: 'media-low.m3u8', bandwidth: 121, programId: 1, codecs: 'bar1', resolution: 'buzz1')
        def stream2 = new StreamAttributes(uri: 'media-high.m3u8', bandwidth: 122, programId: 1, codecs: 'bar2', resolution: 'buzz2')

        and:
        def variantPlaylistKey = 'master-playlist'
        def variantPlaylist = new VariantPlaylist(streams: [stream1, stream2])

        and:
        def media1 = new MediaPlaylist(targetDuration: 11, mediaSequence: 21, version: 3, allowCache: true)
        def media2 = new MediaPlaylist(targetDuration: 12, mediaSequence: 22, version: 4, allowCache: false)

        and:
        def keyPrefix = 'base/'

        when:
        service.addPlaylists(video, keyPrefix, variantPlaylistKey)

        then:
        1 * service.playlistParserService.parseVariantPlaylist(keyPrefix + variantPlaylistKey + '.m3u8') >> variantPlaylist

        and:
        1 * service.playlistParserService.parseMediaPlaylist(keyPrefix + stream1.uri) >> media1
        1 * service.playlistParserService.parseMediaPlaylist(keyPrefix + stream2.uri) >> media2

        and:
        video.available
        video.playlists.size() == 2

        and:
        def playlist1 = (video.playlists as List)[0]
        playlist1.video == video
        playlist1.id != null

        and:
        playlist1.codecs == stream1.codecs
        playlist1.bandwidth == stream1.bandwidth
        playlist1.programId == stream1.programId
        playlist1.resolution == stream1.resolution

        and:
        playlist1.hlsVersion == media1.version
        playlist1.targetDuration == media1.targetDuration
        playlist1.mediaSequence == media1.mediaSequence

        and:
        def playlist2 = (video.playlists as List)[1]
        playlist2.video == video
        playlist2.id != null

        and:
        playlist2.codecs == stream2.codecs
        playlist2.bandwidth == stream2.bandwidth
        playlist2.programId == stream2.programId
        playlist2.resolution == stream2.resolution

        and:
        playlist2.hlsVersion == media2.version
        playlist2.targetDuration == media2.targetDuration
        playlist2.mediaSequence == media2.mediaSequence
    }
}
