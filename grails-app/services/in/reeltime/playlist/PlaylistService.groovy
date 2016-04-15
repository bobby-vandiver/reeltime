package in.reeltime.playlist

import grails.transaction.Transactional
import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.MediaSegment
import in.reeltime.hls.playlist.StreamAttributes
import in.reeltime.hls.playlist.VariantPlaylist
import in.reeltime.hls.playlist.composer.MediaPlaylistComposer
import in.reeltime.hls.playlist.composer.VariantPlaylistComposer
import in.reeltime.video.Video

@Transactional
class PlaylistService {

    def playlistParserService

    void addPlaylists(Video video, String keyPrefix, String variantPlaylistKey) {
        def videoId = video.id
        log.debug("Adding playlists to video [$videoId] with keyPrefix [$keyPrefix] and variantPlaylistKey [$variantPlaylistKey]")

        def variantPath = keyPrefix + variantPlaylistKey + '.m3u8'
        def variantPlaylist = playlistParserService.parseVariantPlaylist(variantPath) as VariantPlaylist

        def playlistUris = []
        def playlists = []

        log.info "Adding variant playlist uri [$variantPath] to tracked uris for video [$videoId]"
        playlistUris << new PlaylistUri(type: PlaylistType.Variant, uri: variantPath).save()

        variantPlaylist.streams.each { stream ->

            def mediaPath = keyPrefix + stream.uri
            def mediaPlaylist = playlistParserService.parseMediaPlaylist(mediaPath) as MediaPlaylist

            log.info "Adding media playlist uri [$mediaPath] to tracked uris for video [$videoId]"
            playlistUris << new PlaylistUri(type: PlaylistType.Media, uri: mediaPath).save()

            def playlist = new Playlist(
                    codecs: stream.codecs,
                    bandwidth: stream.bandwidth,
                    programId: stream.programId,
                    resolution: stream.resolution,
                    hlsVersion: mediaPlaylist.version,
                    targetDuration: mediaPlaylist.targetDuration,
                    mediaSequence: mediaPlaylist.mediaSequence
            ).save()

            log.info("Adding segments to playlist for video [$videoId]")
            mediaPlaylist.segments.eachWithIndex { seg, idx ->
                def segment = new Segment(segmentId: idx, uri: keyPrefix + seg.uri, duration: seg.duration).save()

                log.info("Associating segment [$segment] with playlist [$playlist]")
                new PlaylistSegment(playlist: playlist, segment: segment).save()
            }

            log.info("Adding playlist to video [$videoId]")
            playlists << playlist
        }

        log.info("Making video [$videoId] available for streaming")
        video.available = true

        video.save()

        playlistUris.each { uri ->
            log.info("Associating playlist uri [$uri] with video [$video]")
            new PlaylistUriVideo(playlistUri: uri, video: video).save()
        }

        playlists.each { playlist ->
            log.info("Associating playlist [$playlist] with video [$video]")
            new PlaylistVideo(playlist: playlist, video: video).save()
        }
    }

    String generateVariantPlaylist(Video video) {
        def writer = new StringWriter()
        def streams = video.playlists.collect { p ->

            def uri = video.id + '/' + p.id
            new StreamAttributes(
                    uri: uri,
                    bandwidth: p.bandwidth,
                    programId: p.programId,
                    codecs: p.codecs,
                    resolution: p.resolution
            )
        }
        VariantPlaylistComposer.compose(streams, writer)
        writer.toString()
    }

    String generateMediaPlaylist(Playlist playlist, boolean allowCache) {
        def writer = new StringWriter()
        def segments = playlist.segments.sort()

        def mediaPlaylist = new MediaPlaylist(
                targetDuration: playlist.targetDuration,
                mediaSequence: playlist.mediaSequence,
                version: playlist.hlsVersion,
                allowCache: allowCache,
                segments: segments.collect { s -> new MediaSegment(uri: playlist.id + '/' + s.segmentId, duration: s.duration)}
        )

        MediaPlaylistComposer.compose(mediaPlaylist, writer)
        writer.toString()
    }
}
