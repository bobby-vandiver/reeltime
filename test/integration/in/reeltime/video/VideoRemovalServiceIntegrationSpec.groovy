package in.reeltime.video

import grails.test.spock.IntegrationSpec
import in.reeltime.playlist.Playlist
import in.reeltime.playlist.PlaylistType
import in.reeltime.playlist.Segment
import in.reeltime.reel.Reel
import in.reeltime.reel.ReelVideo
import in.reeltime.transcoder.TranscoderJob
import in.reeltime.user.User
import in.reeltime.maintenance.ResourceRemovalTarget
import spock.lang.Unroll
import test.helper.UserFactory

class VideoRemovalServiceIntegrationSpec extends IntegrationSpec {

    def videoRemovalService
    def pathGenerationService

    User creator
    Reel reel

    String inputBase
    String outputBase

    void setup() {
        creator = UserFactory.createTestUser()
        reel = creator.reels[0]

        inputBase = pathGenerationService.inputBase
        outputBase = pathGenerationService.outputBase
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
        )

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
        if(removeById) {
            videoRemovalService.removeVideoById(videoId)
        }
        else {
            videoRemovalService.removeVideo(video)
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
        ResourceRemovalTarget.findByBaseAndRelative(inputBase, 'something.mp4') != null

        ResourceRemovalTarget.findByBaseAndRelative(outputBase, 'seg1.ts') != null
        ResourceRemovalTarget.findByBaseAndRelative(outputBase, 'seg2.ts') != null

        ResourceRemovalTarget.findByBaseAndRelative(outputBase, 'variant.m3u8') != null
        ResourceRemovalTarget.findByBaseAndRelative(outputBase, 'media.m3u8') != null

        where:
        _   |   removeById
        _   |   false
        _   |   true
    }
}
