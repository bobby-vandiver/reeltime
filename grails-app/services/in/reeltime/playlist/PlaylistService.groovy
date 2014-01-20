package in.reeltime.playlist

import in.reeltime.hls.playlist.MediaPlaylist
import in.reeltime.hls.playlist.VariantPlaylist
import in.reeltime.video.Video

class PlaylistService {

    def playlistParsingService

    def addPlaylists(Video video, String keyPrefix, String variantPlaylistKey) {

        log.debug("Adding playlists to video [${video.id}] with keyPrefix [$keyPrefix] and variantPlaylistKey [$variantPlaylistKey]")

        def variantPath = keyPrefix + variantPlaylistKey
        def variantPlaylist = playlistParsingService.parseVariantPlaylist(variantPath) as VariantPlaylist

        variantPlaylist.streams.each { stream ->

            def mediaPath = keyPrefix + stream.uri
            def mediaPlaylist = playlistParsingService.parseMediaPlaylist(mediaPath) as MediaPlaylist

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
}
