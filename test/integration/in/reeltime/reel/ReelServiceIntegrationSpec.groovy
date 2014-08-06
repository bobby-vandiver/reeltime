package in.reeltime.reel

import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import in.reeltime.video.Video

class ReelServiceIntegrationSpec extends IntegrationSpec {

    def reelService

    def userService
    def clientService

    User owner

    void setup() {
        def client = clientService.createAndSaveClient('clientName', 'clientId', 'clientSecret')
        owner = userService.createAndSaveUser('user', 'password', 'user@test.com', client)
    }

    void "add video the owner created to their reel"() {
        given:
        def video = new Video(creator: owner, title: 'owner created', masterPath: 'somewhere').save()
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
    }
}
