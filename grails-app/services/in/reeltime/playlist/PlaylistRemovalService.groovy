package in.reeltime.playlist

import grails.transaction.Transactional
import in.reeltime.video.Video

@Transactional
class PlaylistRemovalService {

    def resourceRemovalService
    def playlistAndSegmentStorageService

    void removePlaylistsForVideo(Video video) {
        def videoId = video.id
        def playlistAndSegmentBase = playlistAndSegmentStorageService.playlistBase

        log.info "Scheduling removal of playlists for video [$videoId]"

        video.playlistUris.each { playlistUri ->
            resourceRemovalService.scheduleForRemoval(playlistAndSegmentBase, playlistUri.uri)

            PlaylistUriVideo.findByVideoAndPlaylistUri(video, playlistUri).delete()
            playlistUri.delete()
        }

        log.info "Scheduling removal of video segments for video [$videoId]"

        video.playlists.each { playlist ->
            playlist.segments.each { segment ->
                resourceRemovalService.scheduleForRemoval(playlistAndSegmentBase, segment.uri)

                PlaylistSegment.findByPlaylistAndSegment(playlist, segment).delete()
                segment.delete()
            }

            PlaylistVideo.findByVideoAndPlaylist(video, playlist).delete()
            playlist.delete()
        }
    }
}
