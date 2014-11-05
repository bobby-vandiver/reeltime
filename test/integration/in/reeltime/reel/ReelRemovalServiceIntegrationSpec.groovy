package in.reeltime.reel

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.spock.IntegrationSpec
import in.reeltime.user.User
import test.helper.UserFactory
import in.reeltime.exceptions.AuthorizationException

import static in.reeltime.reel.Reel.UNCATEGORIZED_REEL_NAME

class ReelRemovalServiceIntegrationSpec extends IntegrationSpec {

    def reelRemovalService
    def reelService

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
            reelService.addReel(name)
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
}
