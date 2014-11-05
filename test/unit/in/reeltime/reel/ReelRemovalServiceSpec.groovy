package in.reeltime.reel

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import in.reeltime.exceptions.AuthorizationException

@TestFor(ReelRemovalService)
@Mock([Reel])
class ReelRemovalServiceSpec extends Specification {

    ReelAuthorizationService reelAuthorizationService
    ReelVideoManagementService reelVideoManagementService
    AudienceService audienceService

    Reel reel

    void setup() {
        reelAuthorizationService = Mock(ReelAuthorizationService)
        reelVideoManagementService = Mock(ReelVideoManagementService)
        audienceService = Mock(AudienceService)

        service.reelAuthorizationService = reelAuthorizationService
        service.reelVideoManagementService = reelVideoManagementService
        service.audienceService = audienceService

        reel = new Reel()
    }

    void "prepare for removal delegates to services to remove videos and audience members"() {
        when:
        service.prepareReelForRemoval(reel)

        then:
        1 * reelAuthorizationService.currentUserIsReelOwner(reel) >> true
        1 * reelVideoManagementService.removeAllVideosFromReel(reel)
        1 * audienceService.removeAllMembersFromAudience(reel)
    }

    void "only the owner can prepare a reel for removal"() {
        when:
        service.prepareReelForRemoval(reel)

        then:
        def e = thrown(AuthorizationException)
        e.message == "Only the owner of a reel can delete it"

        1 * reelAuthorizationService.currentUserIsReelOwner(reel) >> false
    }
}
