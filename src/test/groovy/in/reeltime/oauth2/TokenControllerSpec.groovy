package in.reeltime.oauth2

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec

@TestFor(TokenController)
class TokenControllerSpec extends AbstractControllerSpec {

    TokenRemovalService tokenRemovalService

    void setup() {
        tokenRemovalService = Mock(TokenRemovalService)
        controller.tokenRemovalService = tokenRemovalService
    }

    void "remove token should attempt to remove token and return 200"() {
        given:
        params.access_token = 'token'

        when:
        controller.revokeAccessToken()

        then:
        assertStatusCode(response, 200)

        and:
        1 * tokenRemovalService.removeAccessToken('token')
    }
}
