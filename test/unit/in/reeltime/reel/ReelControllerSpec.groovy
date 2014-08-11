package in.reeltime.reel

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.InvalidReelNameException
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.exceptions.UserNotFoundException
import in.reeltime.message.LocalizedMessageService
import spock.lang.Unroll

@TestFor(ReelController)
@Mock([Reel])
class ReelControllerSpec extends AbstractControllerSpec {

    ReelService reelService
    LocalizedMessageService localizedMessageService

    void setup() {
        reelService = Mock(ReelService)
        localizedMessageService = Mock(LocalizedMessageService)

        controller.reelService = reelService
        controller.localizedMessageService = localizedMessageService
    }

    void "empty reels list"() {
        given:
        params.username = 'bob'

        when:
        controller.listReels()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 0

        and:
        1 * reelService.listReels('bob') >> []
    }

    void "reels list contains only one reel"() {
        given:
        def reel = new Reel(name: 'foo').save(validate: false)
        assert reel.id > 0

        and:
        params.username = 'bob'

        when:
        controller.listReels()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 1

        and:
        json[0].size() == 2
        json[0].reelId == reel.id
        json[0].name == 'foo'

        and:
        1 * reelService.listReels('bob') >> [reel]
    }

    void "reels list contains multiple reels"() {
        given:
        def reel1 = new Reel(name: 'foo').save(validate: false)
        assert reel1.id > 0

        and:
        def reel2 = new Reel(name: 'bar').save(validate: false)
        reel2.id > 0

        and:
        params.username = 'bob'

        when:
        controller.listReels()

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 2

        and:
        json[0].size() == 2
        json[0].reelId == reel1.id
        json[0].name == 'foo'

        and:
        json[1].size() == 2
        json[1].reelId == reel2.id
        json[1].name == 'bar'

        and:
        1 * reelService.listReels('bob') >> [reel1, reel2]
    }

    void "cannot list reels for unknown user"() {
        given:
        def username = 'someone'
        params.username = username

        and:
        def message = 'unknown username'

        when:
        controller.listReels()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * reelService.listReels(username) >> { throw new UserNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('reel.unknown.username', request.locale) >> message
    }

    void "successfully add a new reel"() {
        given:
        def reelName = 'test-reel-name'
        params.name = reelName

        when:
        controller.addReel()

        then:
        response.status == 201
        response.contentLength == 0

        and:
        1 * reelService.addReel(reelName)
    }

    void "unable to add reel with invalid name"() {
        given:
        def reelName = 'invalid-reel'
        params.name = reelName

        and:
        def message = 'reel bad name'

        when:
        controller.addReel()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * reelService.addReel(reelName) >> { throw new InvalidReelNameException('TEST') }
        1 * localizedMessageService.getMessage('reel.invalid.name', request.locale) >> message
    }

    void "successfully delete a reel"() {
        given:
        def reelId = 8675309
        params.reelId = reelId

        when:
        controller.deleteReel()

        then:
        response.status == 200
        response.contentLength == 0

        and:
        1 * reelService.deleteReel(reelId)
    }

    void "unauthorized delete reel request"() {
        given:
        def message = 'unauthorized request'

        and:
        def reelId = 1234
        params.reelId = reelId

        when:
        controller.deleteReel()

        then:
        assertErrorMessageResponse(response, 403, message)

        and:
        1 * reelService.deleteReel(reelId) >> { throw new AuthorizationException('TEST') }
        1 * localizedMessageService.getMessage('reel.unauthorized', request.locale) >> message
    }

    void "attempt to delete an unknown reel"() {
        given:
        def message = 'unknown reel'

        and:
        def reelId = 9431
        params.reelId = reelId

        when:
        controller.deleteReel()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * reelService.deleteReel(reelId) >> { throw new ReelNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('reel.unknown', request.locale) >> message
    }

    @Unroll
    void "[#paramName] cannot be [#paramValue] for action [#actionName]"() {
        given:
        def message = 'TEST'

        and:
        params."$paramName" = paramValue

        when:
        controller."$actionName"()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * localizedMessageService.getMessage(code, request.locale) >> message

        where:
        paramName   |   paramValue  |   actionName      |   code
        'username'  |   null        |   'listReels'     |   'reel.username.required'
        'username'  |   ''          |   'listReels'     |   'reel.username.required'
        'name'      |   null        |   'addReel'       |   'reel.name.required'
        'name'      |   ''          |   'addReel'       |   'reel.name.required'
        'reelId'    |   null        |   'deleteReel'    |   'reel.id.required'
        'reelId'    |   ''          |   'deleteReel'    |   'reel.id.required'
    }
}
