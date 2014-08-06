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

    private User selectUser(boolean selectOwner) {
        return selectOwner ? owner : notOwner
    }
}
