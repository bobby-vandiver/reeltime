package in.reeltime.playlist

import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.MediaSegment
import in.reeltime.hls.playlist.StreamAttributes
import in.reeltime.hls.playlist.VariantPlaylist
import in.reeltime.video.Video
import in.reeltime.hls.playlist.composer.MediaPlaylistComposer
import in.reeltime.hls.playlist.composer.VariantPlaylistComposer

class PlaylistService {

    def playlistParserService

    def addPlaylists(Video video, String keyPrefix, String variantPlaylistKey) {

        log.debug("Adding playlists to video [${video.id}] with keyPrefix [$keyPrefix] and variantPlaylistKey [$variantPlaylistKey]")

        def variantPath = keyPrefix + variantPlaylistKey
        def variantPlaylist = playlistParserService.parseVariantPlaylist(variantPath) as VariantPlaylist

        variantPlaylist.streams.each { stream ->

            def mediaPath = keyPrefix + stream.uri
            def mediaPlaylist = playlistParserService.parseMediaPlaylist(mediaPath) as MediaPlaylist

            def playlist = new Playlist(
                    codecs: stream.codecs,
                    bandwidth: stream.bandwidth,
                    programId: stream.programId,
                    resolution: stream.resolution,
                    hlsVersion: mediaPlaylist.version,
                    targetDuration: mediaPlaylist.targetDuration,
                    mediaSequence: mediaPlaylist.mediaSequence
            )

            log.info("Adding segments to playlist for video [${video.id}]")
            mediaPlaylist.segments.eachWithIndex { seg, idx ->
                playlist.addToSegments(segmentId: idx, uri: seg.uri, duration: seg.duration)
            }

            log.info("Adding playlist to video [${video.id}]")
            video.addToPlaylists(playlist)
        }

        video.save()
    }

    def generateVariantPlaylist(Video video) {

        def writer = new StringWriter()
        def streams = video.playlists.collect { p ->

            new StreamAttributes(
                    uri: p.id,
                    bandwidth: p.bandwidth,
                    programId: p.programId,
                    codecs: p.codecs,
                    resolution: p.resolution
            )
        }
        VariantPlaylistComposer.compose(streams, writer)
        writer.toString()
    }

    def generateMediaPlaylist(Playlist playlist, boolean allowCache) {

        def writer = new StringWriter()
        def segments = playlist.segments.sort()

        def mediaPlaylist = new MediaPlaylist(
                targetDuration: playlist.targetDuration,
                mediaSequence: playlist.mediaSequence,
                version: playlist.hlsVersion,
                allowCache: allowCache,
                segments: segments.collect { s -> new MediaSegment(uri: s.id, duration: s.duration)}
        )

        MediaPlaylistComposer.compose(mediaPlaylist, writer)
        writer.toString()
    }
}
