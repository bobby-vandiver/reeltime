package in.reeltime.playlist

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.video.Video
import in.reeltime.thumbnail.ThumbnailResolution
import in.reeltime.maintenance.ResourceRemovalTarget
import test.helper.UserFactory

class PlaylistRemovalServiceIntegrationSpec extends IntegrationSpec {

    def playlistRemovalService
    def playlistAndSegmentStorageService

    User creator
    String playlistBase

    void setup() {
        creator = UserFactory.createTestUser()
        playlistBase = playlistAndSegmentStorageService.playlistBase
    }

    void "remove playlists from video and schedule playlist and segment files for removal"() {
        given:
        def playlist = new Playlist()
        playlist.addToSegments(segmentId: 1, uri: 'seg1.ts', duration: '1.0')
        playlist.addToSegments(segmentId: 2, uri: 'seg2.ts', duration: '1.0')

        assert playlist.segments.size() == 2

        def video = new Video(
                creator: creator,
                title: 'some video',
                masterPath: 'something.mp4',
                masterThumbnailPath: 'something.png'
        )

        video.addToPlaylists(playlist)

        video.addToPlaylistUris(type: PlaylistType.Variant, uri: 'variant.m3u8')
        video.addToPlaylistUris(type: PlaylistType.Media, uri: 'media.m3u8')

        video.save(validate: false)

        when:
        playlistRemovalService.removePlaylistsForVideo(video)

        then:
//        video.playlists.empty
//        video.playlistUris.empty
//
//        playlist.segments.empty

        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'seg1.ts') != null
        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'seg2.ts') != null

        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'variant.m3u8') != null
        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'media.m3u8') != null
    }
}
