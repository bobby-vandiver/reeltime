package in.reeltime.reel

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.video.Video
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
    void "add video to reel when video creator is reel owner [#videoCreatorIsReelOwner]"() {
        given:
        def creator = selectUser(videoCreatorIsReelOwner)

        def video = new Video(creator: creator, title: 'owner created', masterPath: 'somewhere').save()
        def videoId = video.id

        and:
        def reel = reelService.createReel(owner, 'some reel').save()
        def reelId = reel.id

        when:
        reelService.addVideo(reelId, videoId)

        then:
        def fetchedReel = Reel.findById(reelId)
        fetchedReel != null

        and:
        fetchedReel.videos.size() == 1
        fetchedReel.videos.contains(video)

        where:
        _   |   videoCreatorIsReelOwner
        _   |   true
        _   |   false
    }

    @Unroll
    void "reel contains [#count] videos"() {
        given:
        def reel = reelService.createReel(owner, "reel with $count videos")
        def videos = createVideos(reel, count)

        and:
        reel.save()

        when:
        def list = reelService.listVideos(reel.id)

        then:
        list.size() == videos.size()

        and:
        assertVideosAreInList(list, videos)

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

    private Collection<Video> createVideos(Reel reel, int count) {
        def videos = []
        for(int i = 0; i < count; i++) {
            def video = new Video(creator: owner, title: "test video $i", masterPath: "path $i").save()
            videos << video
            reel.addToVideos(video)
        }
        return videos
    }

    private static void assertVideosAreInList(Collection<Video> actual, Collection<Video> expected) {
        expected.each { video ->
            assert actual.contains(video)
        }
    }
}
