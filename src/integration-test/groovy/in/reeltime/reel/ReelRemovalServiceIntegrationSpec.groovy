package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.user.User
import in.reeltime.video.Video
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import in.reeltime.test.factory.UserFactory
import in.reeltime.test.factory.VideoFactory

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

@Integration
@Rollback
class ReelRemovalServiceIntegrationSpec extends Specification {

    @Autowired
    ReelRemovalService reelRemovalService

    @Autowired
    ReelCreationService reelCreationService

    @Autowired
    ReelVideoManagementService reelVideoManagementService

    @Autowired
    AudienceService audienceService

    User owner
    User notOwner

    void "do not allow a reel to be deleted if owner is not current user"() {
        given:
        createOwner()
        createNotOwner()

        and:
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
        createOwner()

        and:
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
        createOwner()

        and:
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
        createOwner()
        createNotOwner()

        and:
        def uncategorizedReel = owner.reels[0]
        def video = VideoFactory.createVideo(notOwner, 'test')

        SpringSecurityUtils.doWithAuth(notOwner.username) {
            audienceService.addCurrentUserToAudience(uncategorizedReel.id)
        }

        when:
        def targetUser = owner

        and:
        SpringSecurityUtils.doWithAuth(owner.username) {
            reelRemovalService.removeAllReelsForUser(targetUser)
        }

        then:
        owner.reels.size() == 0

        and:
        AudienceMember.findAllByReel(uncategorizedReel)*.member.size() == 0
        UserReel.findAllByOwner(owner).size() == 0

        and:
        Video.findById(video.id) != null

        and:
        ReelVideo.findAllByReelAndVideo(uncategorizedReel, video).size() == 0
    }

    void "removing all reels deletes the uncategorized reel and deletes others"() {
        given:
        createOwner()
        createNotOwner()

        and:
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
            reelRemovalService.removeAllReelsForUser(targetUser)
        }

        then:
        owner.reels.size() == 0

        and:
        !owner.hasReel(UNCATEGORIZED_REEL_NAME)
        !owner.hasReel('other')

        and:
        AudienceMember.findAllByReel(uncategorizedReel)*.member.size() == 0
        UserReel.findAllByOwner(owner).size() == 0

        and:
        Video.findById(video.id) != null

        and:
        ReelVideo.findAllByReelAndVideo(uncategorizedReel, video).size() == 0
    }

    private void createOwner() {
        owner = UserFactory.createUser('theOwner')
    }

    private void createNotOwner() {
        notOwner = UserFactory.createUser('notTheOwner')
    }
}
