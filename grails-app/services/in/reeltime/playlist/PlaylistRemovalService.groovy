package in.reeltime.playlist

import in.reeltime.video.Video

class PlaylistRemovalService {

    def resourceRemovalService
    def playlistAndSegmentStorageService

    void removePlaylistsForVideo(Video video) {
        def videoId = video.id
        def playlistAndSegmentBase = playlistAndSegmentStorageService.playlistBase

        log.info "Scheduling removal of playlists for video [$videoId]"
        video.playlistUris.each { playlistUri ->
            resourceRemovalService.scheduleForRemoval(playlistAndSegmentBase, playlistUri.uri)
        }
//        video.playlistUris.clear()

        log.info "Scheduling removal of video segments for video [$videoId]"
        video.playlists.each { playlist ->
            playlist.segments.each { segment ->
                resourceRemovalService.scheduleForRemoval(playlistAndSegmentBase, segment.uri)
            }
//            playlist.segments.clear()
        }
//        video.playlists.clear()
    }
}
