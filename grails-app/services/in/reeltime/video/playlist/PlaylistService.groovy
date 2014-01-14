package in.reeltime.video.playlist

import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.VariantPlaylist
import in.reeltime.video.Video

class PlaylistService {

    def playlistParsingService

    def addPlaylists(Video video, String variantPlaylistKey) {

        def variantPlaylist = playlistParsingService.parseVariantPlaylist(variantPlaylistKey) as VariantPlaylist

        variantPlaylist.streams.each { stream ->

            def mediaPlaylist = playlistParsingService.parseMediaPlaylist(stream.uri) as MediaPlaylist

            def playlist = new Playlist(
                    codecs: stream.codecs,
                    bandwidth: stream.bandwidth,
                    programId: stream.programId,
                    resolution: stream.resolution,
                    hlsVersion: mediaPlaylist.version,
                    targetDuration: mediaPlaylist.targetDuration,
                    mediaSequence: mediaPlaylist.mediaSequence
            )

            mediaPlaylist.segments.eachWithIndex { seg, idx ->
                playlist.addToSegments(segmentId: idx, uri: seg.uri, duration: seg.duration)
            }

            video.addToPlaylists(playlist)
            video.save()
        }
    }
}
