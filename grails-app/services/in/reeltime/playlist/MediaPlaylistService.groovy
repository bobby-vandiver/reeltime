package in.reeltime.playlist

import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.MediaSegment
import in.reeltime.hls.playlist.composer.MediaPlaylistComposer

class MediaPlaylistService {

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
