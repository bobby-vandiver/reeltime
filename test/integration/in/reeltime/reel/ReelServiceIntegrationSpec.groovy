package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.video.Video
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.exceptions.VideoNotFoundException
import in.reeltime.exceptions.AuthorizationException
import spock.lang.Unroll

class ReelServiceIntegrationSpec extends IntegrationSpec {

    def reelService

    def userService
    def clientService

    User owner
    User notOwner

    void setup() {
        def ownerClient = clientService.createAndSaveClient('cname1', 'cid1', 'secret')
        owner = userService.createAndSaveUser('someone', 'password', 'someone@test.com', ownerClient)

        def notOwnerClient = clientService.createAndSaveClient('cname2', 'cid2', 'secret')
        notOwner = userService.createAndSaveUser('nobody', 'password', 'nobody@test.com', notOwnerClient)
    }

    @Unroll
    void "add video to reel when video creator is reel owner"() {
        given:
        def video = createAndSaveVideo(owner)
        def videoId = video.id

        and:
        def reel = reelService.createReel(owner, 'some reel').save()
        def reelId = reel.id

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelService.addVideo(reelId, videoId)
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
        def reel = reelService.createReel(owner, 'some reel').save()
        def reelId = reel.id

        when:
        SpringSecurityUtils.doWithAuth(notOwner.username) {
            reelService.addVideo(reelId, videoId)
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
            reelService.addVideo(1234, video.id)
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
            reelService.addVideo(reelId, 567801)
        }

        then:
        def e = thrown(VideoNotFoundException)
        e.message == "Video [567801] not found"
    }

    @Unroll
    void "list all reels belonging to specified user -- user has [#count] reels total"() {
        given:
        def username = owner.username
        def reels = createReels(count)

        when:
        def list = reelService.listReels(username)

        then:
        assertListsContainSameElements(list, reels)

        where:
        _   |   count
        _   |   0
        _   |   1
        _   |   2
        _   |   5
        _   |   10
        _   |   100
    }

    @Unroll
    void "reel contains [#count] videos"() {
        given:
        def reel = reelService.createReel(owner, "reel with $count videos")
        def videos = createVideos(reel, count)

        when:
        def list = reelService.listVideos(reel.id)

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

    private User selectUser(boolean selectOwner) {
        return selectOwner ? owner : notOwner
    }

    private Collection<Reel> createReels(int count) {
        def reels = owner.reels
        def initialCount = reels.size()

        for(int i = initialCount; i < count; i++) {
            def reel = reelService.createReel(owner, "reel $i")
            reels << reel
            owner.addToReels(reel)
        }
        owner.save()
        return reels
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

    private static void assertListsContainSameElements(Collection<?> actual, Collection<?> expected) {
        assert actual.size() == expected.size()

        expected.each { element ->
            assert actual.contains(element)
        }
    }
}
