package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.activity.UserReelVideoActivity
import in.reeltime.user.User
import in.reeltime.video.Video
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.exceptions.AuthorizationException
import spock.lang.Unroll
import test.helper.ReelFactory
import test.helper.UserFactory
import test.helper.VideoFactory
import in.reeltime.activity.ActivityType

class ReelVideoManagementServiceIntegrationSpec extends IntegrationSpec {

    def reelCreationService
    def reelVideoManagementService

    def activityService

    User owner
    User notOwner

    void setup() {
        owner = UserFactory.createUser('theOwner')
        notOwner = UserFactory.createUser('notTheOwner')
    }

    void "do not list videos that are not available for streaming"() {
        given:
        def unavailable = VideoFactory.createVideo(owner, 'unavailable', false)

        and:
        def reel = owner.reels[0]
        def reelId = reel.id

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideoToReel(reel, unavailable)
        }

        when:
        def list = reelVideoManagementService.listVideosInReel(reelId, 1)

        then:
        list.size() == 0
    }

    void "list videos in reel by page from newest to oldest"() {
        given:
        def savedMaxVideosPerPage = reelVideoManagementService.maxVideosPerPage
        reelVideoManagementService.maxVideosPerPage = 2

        and:
        def reel = owner.reels[0]
        def reelId = reel.id

        and:
        def first = VideoFactory.createVideoAndWait(owner, 'first', 1000)
        def second = VideoFactory.createVideoAndWait(owner, 'second', 1000)
        def third = VideoFactory.createVideoAndWait(owner, 'third', 1000)

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideoToReel(reel, first)
            reelVideoManagementService.addVideoToReel(reel, second)
            reelVideoManagementService.addVideoToReel(reel, third)
        }

        when:
        def pageOne = reelVideoManagementService.listVideosInReel(reelId, 1)

        then:
        pageOne.size() == 2

        and:
        pageOne[0] == third
        pageOne[1] == second

        when:
        def pageTwo = reelVideoManagementService.listVideosInReel(reelId, 2)

        then:
        pageTwo.size() == 1

        and:
        pageTwo[0] == first

        cleanup:
        reelVideoManagementService.maxVideosPerPage = savedMaxVideosPerPage
    }

    @Unroll
    void "add video to reel when video creator is reel owner"() {
        given:
        def video = createAndSaveVideo(owner)
        def videoId = video.id

        and:
        def reel = ReelFactory.createReel(owner, 'some reel')
        def reelId = reel.id

        activityService.reelCreated(owner, reel)

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideo(reelId, videoId)
        }

        then:
        ReelVideo.findByReelAndVideo(reel, video) != null

        and:
        def activities = activityService.findActivities([], [reel])
        activities.size() == 2

        and:
        activities.find { it.type == ActivityType.CreateReel } != null

        and:
        def activity = activities.find { it.type == ActivityType.AddVideoToReel }
        activity instanceof UserReelVideoActivity

        and:
        activity.user == owner
        activity.reel == reel
        activity.video == video
    }

    void "add same video to multiple reels"() {
        given:
        def video = createAndSaveVideo(owner)
        def videoId = video.id

        and:
        def reel1 = ReelFactory.createReel(owner, 'reel1')
        def reel1Id = reel1.id

        and:
        def reel2 = ReelFactory.createReel(owner, 'reel2')
        def reel2Id = reel2.id

        and:
        activityService.reelCreated(owner, reel1)
        activityService.reelCreated(owner, reel2)

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideo(reel1Id, videoId)
            reelVideoManagementService.addVideo(reel2Id, videoId)
        }

        then:
        ReelVideo.findByReelAndVideo(reel1, video) != null
        ReelVideo.findByReelAndVideo(reel2, video) != null
    }

    void "only the owner of the reel can add videos to a reel"() {
        given:
        def video = createAndSaveVideo(owner)
        def videoId = video.id

        and:
        def reel = ReelFactory.createReel(owner, 'some reel')
        def reelId = reel.id

        when:
        SpringSecurityUtils.doWithAuth(notOwner.username) {
            reelVideoManagementService.addVideo(reelId, videoId)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == "Only the owner of a reel can add videos to it"
    }

    void "throw if adding a video to a reel that does not exist"() {
        given:
        def video = createAndSaveVideo(owner)

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideo(1234, video.id)
        }

        then:
        def e = thrown(ReelNotFoundException)
        e.message == "Reel [1234] not found"
    }

    void "throw if adding a non existent video to a reel"() {
        given:
        def reelId = owner.reels[0].id

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideo(reelId, 567801)
        }

        then:
        def e = thrown(VideoNotFoundException)
        e.message == "Video [567801] not found"
    }

    void "throw if adding a video to a reel it already belongs to"() {
        given:
        def video = createAndSaveVideo(owner)
        def videoId = video.id

        and:
        def reel = ReelFactory.createReel(owner, 'some reel')
        def reelId = reel.id

        activityService.reelCreated(owner, reel)

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideo(reelId, videoId)
        }

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideo(reelId, videoId)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == "Cannot add a video to a reel multiple times"
    }

    void "attempt to remove a video from a reel that does not belong to the current user"() {
        given:
        def reelId = owner.reels[0].id
        def videoId = createAndSaveVideo(owner).id

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideo(reelId, videoId)
        }

        when:
        SpringSecurityUtils.doWithAuth(notOwner.username) {
            reelVideoManagementService.removeVideo(reelId, videoId)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == "Only the owner of a reel can remove videos from it"
    }

    void "attempt to remove a video that does not belong to the reel"() {
        given:
        def reelId = owner.reels[0].id
        def videoId = createAndSaveVideo(owner).id

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.removeVideo(reelId, videoId)
        }

        then:
        def e = thrown(VideoNotFoundException)
        e.message == "Reel [$reelId] does not contain video [$videoId]"
    }

    void "attempt to remove an unknown video from the reel"() {
        given:
        def reelId = owner.reels[0].id
        def videoId = 123456789

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.removeVideo(reelId, videoId)
        }

        then:
        thrown(VideoNotFoundException)
    }

    void "attempt to remove a video from an unknown reel"() {
        given:
        def reelId = 8675309
        def videoId = createAndSaveVideo(owner).id

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.removeVideo(reelId, videoId)
        }

        then:
        thrown(ReelNotFoundException)
    }

    void "removing a video from a reel does not delete the video instance"() {
        given:
        def reelId = owner.reels[0].id
        def videoId = createAndSaveVideo(owner).id

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideo(reelId, videoId)
        }

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.removeVideo(reelId, videoId)
        }

        then:
        def video = Video.findById(videoId)
        video != null

        and:
        def reel = Reel.findById(reelId)
        reel != null

        and:
        ReelVideo.findByReelAndVideo(reel, video) == null
    }

    void "remove all videos from reel"() {
        given:
        def reelName = 'another reel'
        def reel

        def ownerVideo = VideoFactory.createVideo(owner, 'owner video')
        def notOwnerVideo = VideoFactory.createVideo(notOwner, 'not owner video')

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reel = reelCreationService.addReel(reelName)

            reelVideoManagementService.addVideoToReel(reel, ownerVideo)
            reelVideoManagementService.addVideoToReel(reel, notOwnerVideo)
        }

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.removeAllVideosFromReel(reel)
        }

        then:
        ReelVideo.findByReelAndVideo(reel, ownerVideo) == null
        ReelVideo.findByReelAndVideo(reel, notOwnerVideo) == null

        and:
        Reel.findById(reel.id) != null

        and:
        Video.findById(ownerVideo.id) != null
        Video.findById(notOwnerVideo.id) != null
    }

    void "remove video from all reels"() {
        given:
        def video = createAndSaveVideo(owner)
        def videoId = video.id

        and:
        def ownerReelId = owner.reels[0].id
        def notOwnerReelId = notOwner.reels[0].id

        and:
        addVideoToReelForUser(ownerReelId, videoId, owner.username)
        addVideoToReelForUser(notOwnerReelId, videoId, notOwner.username)

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.removeVideoFromAllReels(video)
        }

        then:
        assertVideoInReel(ownerReelId, videoId, false)
        assertVideoInReel(notOwnerReelId, videoId, false)

        and:
        def retrievedVideo = Video.findById(videoId)
        !retrievedVideo.available
    }

    private void addVideoToReelForUser(Long reelId, Long videoId, String username) {
        assert reelId > 0
        assert videoId > 0

        SpringSecurityUtils.doWithAuth(username) {
            reelVideoManagementService.addVideo(reelId, videoId)
        }
        assertVideoInReel(reelId, videoId, true)
    }

    private void assertVideoInReel(Long reelId, Long videoId, boolean shouldContain) {
        def video = Video.findById(videoId)
        def reel = Reel.findById(reelId)

        def list = reelVideoManagementService.listVideosInReel(reelId, 1)
        assert list.contains(video) == shouldContain

        def reelVideo = ReelVideo.findByReelAndVideo(reel, video)
        assert (reelVideo != null) == shouldContain
    }

    @Unroll
    void "reel contains [#count] videos"() {
        given:
        def reel = ReelFactory.createReel(owner, "reel with $count videos")
        def videos = createVideos(reel, count)

        when:
        def list = reelVideoManagementService.listVideosInReel(reel.id, 1)

        then:
        assertListsContainSameElements(list, videos)

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
        _   |   10
    }

    private Collection<Video> createVideos(Reel reel, int count) {
        def videos = []
        for(int i = 0; i < count; i++) {
            def video = createAndSaveVideo(owner, reel, "test video $i", "path $i")
            videos << video
            new ReelVideo(reel: reel, video: video).save()
        }
        reel.save()
        return videos
    }

    private static Video createAndSaveVideo(User creator, Reel reel = null, String title = 'some video', String path = 'somewhere') {
        if(!reel) {
            reel = creator.reels[0]
        }
        new Video(creator: creator, title: title, masterPath: path, available: true).save()
    }

    // TODO: Pull into helper class
    private static void assertListsContainSameElements(Collection<?> actual, Collection<?> expected) {
        assert actual.size() == expected.size()

        expected.each { element ->
            assert actual.contains(element)
        }
    }
}
