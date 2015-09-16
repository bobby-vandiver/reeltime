package in.reeltime.video

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.playlist.Playlist
import in.reeltime.playlist.PlaylistType
import in.reeltime.playlist.Segment
import in.reeltime.reel.Reel
import in.reeltime.reel.ReelVideo
import in.reeltime.transcoder.TranscoderJob
import in.reeltime.user.User
import in.reeltime.maintenance.ResourceRemovalTarget
import in.reeltime.thumbnail.ThumbnailResolution
import in.reeltime.exceptions.AuthorizationException
import spock.lang.Unroll
import test.helper.UserFactory

class VideoRemovalServiceIntegrationSpec extends IntegrationSpec {

    def videoRemovalService

    def videoStorageService
    def thumbnailStorageService
    def playlistAndSegmentStorageService

    User creator
    Reel reel

    String videoBase
    String thumbnailBase
    String playlistBase

    void setup() {
        creator = UserFactory.createTestUser()
        reel = creator.reels[0]

        videoBase = videoStorageService.videoBase
        thumbnailBase = thumbnailStorageService.thumbnailBase
        playlistBase = playlistAndSegmentStorageService.playlistBase
    }

    void "video owner must be the currently logged in user requesting deletion"() {
        given:
        def notCreator = UserFactory.createUser('notCreator')

        and:
        def video = new Video(
                creator: creator,
                title: 'some video',
                masterPath: 'something.mp4',
                masterThumbnailPath: 'something.png'
        ).save()

        when:
        SpringSecurityUtils.doWithAuth(notCreator.username) {
            videoRemovalService.removeVideo(video)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == 'Only the creator of a video can delete it'
    }

    @Unroll
    void "remove video by id [#removeById] successfully and schedule resources for removal"() {
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

        video.addToThumbnails(resolution: ThumbnailResolution.RESOLUTION_1X, uri: 'thumbnail-1x')
        video.addToThumbnails(resolution: ThumbnailResolution.RESOLUTION_2X, uri: 'thumbnail-2x')
        video.addToThumbnails(resolution: ThumbnailResolution.RESOLUTION_3X, uri: 'thumbnail-3x')

        video.addToPlaylists(playlist)

        video.addToPlaylistUris(type: PlaylistType.Variant, uri: 'variant.m3u8')
        video.addToPlaylistUris(type: PlaylistType.Media, uri: 'media.m3u8')

        video.save(validate: false)

        def transcoderJob = new TranscoderJob(video: video, jobId: '1234567890123-ABCDEF').save()

        and:
        [playlist, reel, transcoderJob, video].each {
            println "Asserting id for [$it]"
            assert it?.id > 0
        }
        and:
        def videoId = video.id
        def playlistId = playlist.id

        def segment1Id = playlist.segments[0].id
        def segment2Id = playlist.segments[1].id

        def reelId = reel.id
        def transcoderJobId = transcoderJob.id

        when:
        SpringSecurityUtils.doWithAuth(creator.username) {
            if (removeById) {
                videoRemovalService.removeVideoById(videoId)
            }
            else {
                videoRemovalService.removeVideo(video)
            }
        }

        then:
        Video.findById(videoId) == null
        Playlist.findById(playlistId) == null

        Segment.findById(segment1Id) == null
        Segment.findById(segment2Id) == null

        and:
        Reel.findById(reelId) != null
        ReelVideo.findByReelAndVideo(reel, video) == null

        and:
        TranscoderJob.findById(transcoderJobId) == null

        and:
        ResourceRemovalTarget.findByBaseAndRelative(videoBase, 'something.mp4') != null
        ResourceRemovalTarget.findByBaseAndRelative(thumbnailBase, 'something.png') != null

        ResourceRemovalTarget.findByBaseAndRelative(thumbnailBase, 'thumbnail-1x') != null
        ResourceRemovalTarget.findByBaseAndRelative(thumbnailBase, 'thumbnail-2x') != null
        ResourceRemovalTarget.findByBaseAndRelative(thumbnailBase, 'thumbnail-3x') != null

        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'seg1.ts') != null
        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'seg2.ts') != null

        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'variant.m3u8') != null
        ResourceRemovalTarget.findByBaseAndRelative(playlistBase, 'media.m3u8') != null

        where:
        _   |   removeById
        _   |   false
        _   |   true
    }
}
