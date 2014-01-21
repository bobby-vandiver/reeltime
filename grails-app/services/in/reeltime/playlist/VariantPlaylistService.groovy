package in.reeltime.playlist

import in.reeltime.hls.playlist.StreamAttributes
import in.reeltime.video.Video
import in.reeltime.hls.playlist.composer.VariantPlaylistComposer

class VariantPlaylistService {

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
}
