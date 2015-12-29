package in.reeltime.playlist

import grails.test.spock.IntegrationSpec
import in.reeltime.maintenance.ResourceRemovalTarget
import in.reeltime.user.User
import in.reeltime.video.Video
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
        def playlist = new Playlist().save()

        def segment1 = new Segment(segmentId: 1, uri: 'seg1.ts', duration: '1.0').save()
        def segment2 = new Segment(segmentId: 2, uri: 'seg2.ts', duration: '1.0').save()

        new PlaylistSegment(playlist: playlist, segment: segment1).save()
        new PlaylistSegment(playlist: playlist, segment: segment2).save()

        assert playlist.segments.size() == 2

        def video = new Video(
                creator: creator,
                title: 'some video',
                masterPath: 'something.mp4',
                masterThumbnailPath: 'something.png'
        )

        video.save()

        and:
        new PlaylistVideo(playlist: playlist, video: video).save()

        def playlistUri1 = new PlaylistUri(type: PlaylistType.Variant, uri: 'variant.m3u8').save()
        def playlistUri2 = new PlaylistUri(type: PlaylistType.Media, uri: 'media.m3u8').save()

        new PlaylistUriVideo(playlistUri: playlistUri1, video: video).save()
        new PlaylistUriVideo(playlistUri: playlistUri2, video: video).save()

        and:
        def playlistId = playlist.id

        def segment1Id = segment1.id
        def segment2Id = segment2.id

        def playlistUri1Id = playlistUri1.id
        def playlistUri2Id = playlistUri2.id

        when:
        playlistRemovalService.removePlaylistsForVideo(video)

        then:
        Playlist.findById(playlistId) == null

        Segment.findById(segment1Id) == null
        Segment.findById(segment2Id) == null

        PlaylistUri.findById(playlistUri1Id) == null
        PlaylistUri.findById(playlistUri2Id) == null

        PlaylistUriVideo.findAllByVideo(video).empty
        PlaylistVideo.findAllByVideo(video).empty

        PlaylistSegment.findAllByPlaylist(playlist).empty

        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'seg1.ts') != null
        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'seg2.ts') != null

        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'variant.m3u8') != null
        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'media.m3u8') != null
    }
}
