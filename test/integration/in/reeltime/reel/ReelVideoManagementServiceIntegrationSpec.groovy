package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.video.Video
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.exceptions.AuthorizationException
import spock.lang.Unroll

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

class ReelVideoManagementServiceIntegrationSpec extends IntegrationSpec {

    def reelService
    def reelVideoManagementService

    def userService
    def clientService

    User owner
    User notOwner

    void setup() {
        def ownerRequiredReel = reelService.createReel(UNCATEGORIZED_REEL_NAME)
        def ownerClient = clientService.createAndSaveClient('cname1', 'cid1', 'secret')
        owner = userService.createAndSaveUser('theOwner', 'password', 'someone@test.com', ownerClient, ownerRequiredReel)

        def nowOwnerRequiredReel = reelService.createReel(UNCATEGORIZED_REEL_NAME)
        def notOwnerClient = clientService.createAndSaveClient('cname2', 'cid2', 'secret')
        notOwner = userService.createAndSaveUser('notTheOwner', 'password', 'nobody@test.com', notOwnerClient, nowOwnerRequiredReel)
    }

    @Unroll
    void "add video to reel when video creator is reel owner"() {
        given:
        def video = createAndSaveVideo(owner)
        def videoId = video.id

        and:
        def reel = reelService.createReelForUser(owner, 'some reel').save()
        def reelId = reel.id

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelVideoManagementService.addVideo(reelId, videoId)
        }

        then:
        def fetchedReel = Reel.findById(reelId)
        fetchedReel != null

        and:
        fetchedReel.videos.size() == 1
        fetchedReel.videos.contains(video)
    }

    void "only the owner of the reel can add videos to a reel"() {
        given:
        def video = createAndSaveVideo(owner)
        def videoId = video.id

        and:
        def reel = reelService.createReelForUser(owner, 'some reel').save()
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
        !reel.containsVideo(video)
    }

    @Unroll
    void "reel contains [#count] videos"() {
        given:
        def reel = reelService.createReelForUser(owner, "reel with $count videos").save()
        def videos = createVideos(reel, count)

        when:
        def list = reelVideoManagementService.listVideos(reel.id)

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
            def video = createAndSaveVideo(owner, "test video $i", "path $i")
            videos << video
            reel.addToVideos(video)
        }
        reel.save()
        return videos
    }

    private static Video createAndSaveVideo(User creator, String title = 'some video', String path = 'somewhere') {
        new Video(creator: creator, title: title, masterPath: path).save()
    }

    // TODO: Pull into helper class
    private static void assertListsContainSameElements(Collection<?> actual, Collection<?> expected) {
        assert actual.size() == expected.size()

        expected.each { element ->
            assert actual.contains(element)
        }
    }
}