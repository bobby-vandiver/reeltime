package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import test.helper.UserFactory
import in.reeltime.exceptions.AuthorizationException
import test.helper.VideoFactory
import in.reeltime.video.Video

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

class ReelRemovalServiceIntegrationSpec extends IntegrationSpec {

    def reelRemovalService
    def reelCreationService

    def reelVideoManagementService
    def audienceService

    User owner
    User notOwner

    void setup() {
        owner = UserFactory.createUser('theOwner')
        notOwner = UserFactory.createUser('notTheOwner')
    }

    void "do not allow a reel to be deleted if owner is not current user"() {
        given:
        def reel = owner.reels[0]

        when:
        SpringSecurityUtils.doWithAuth(notOwner.username) {
            reelRemovalService.removeReel(reel)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == "Only the owner of a reel can delete it"
    }

    void "do not allow the uncategorized reel to be deleted"() {
        given:
        def reel = owner.reels[0]
        assert reel.name == UNCATEGORIZED_REEL_NAME

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelRemovalService.removeReel(reel)
        }

        then:
        def e = thrown(AuthorizationException)
        e.message == "The Uncategorized reel cannot be deleted"
    }

    void "allow the owner to delete the reel"() {
        given:
        def name = 'another reel'

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelCreationService.addReel(name)
        }

        and:
        def reel = Reel.findByName(name)
        def reelId = reel.id

        when:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelRemovalService.removeReel(reel)
        }

        then:
        Reel.findById(reelId) == null
    }

    void "remove all when user only has uncategorized reel"() {
        given:
        def uncategorizedReel = owner.reels[0]
        def video = VideoFactory.createVideo(notOwner, 'test')

        SpringSecurityUtils.doWithAuth(notOwner.username) {
            audienceService.addCurrentUserToAudience(uncategorizedReel.id)
        }

        when:
        def targetUser = owner

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelRemovalService.removeReelsForUser(targetUser)
        }

        then:
        owner.reels.size() == 1
        owner.hasReel(UNCATEGORIZED_REEL_NAME)

        and:
        def audience = Audience.findByReel(uncategorizedReel)
        audience.members.size() == 0

        and:
        Video.findById(video.id) != null

        and:
        ReelVideo.findAllByReelAndVideo(uncategorizedReel, video).size() == 0
    }

    void "removing all reels only removes videos and audience members from the uncategorized reel and deletes others"() {
        given:
        def uncategorizedReel = owner.reels[0]
        def video = VideoFactory.createVideo(notOwner, 'test')

        SpringSecurityUtils.doWithAuth(notOwner.username) {
            audienceService.addCurrentUserToAudience(uncategorizedReel.id)
        }

        def otherReel
        SpringSecurityUtils.doWithAuth(owner.username) {
            otherReel = reelCreationService.addReel('other')
            reelVideoManagementService.addVideoToReel(uncategorizedReel, video)
        }

        and:
        assert owner.reels.size() == 2

        when:
        def targetUser = owner

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelRemovalService.removeReelsForUser(targetUser)
        }

        then:
        owner.reels.size() == 1

        and:
        owner.hasReel(UNCATEGORIZED_REEL_NAME)
        !owner.hasReel('other')

        and:
        def audience = Audience.findByReel(uncategorizedReel)
        audience.members.size() == 0

        and:
        Video.findById(video.id) != null

        and:
        ReelVideo.findAllByReelAndVideo(uncategorizedReel, video).size() == 0
    }
}
