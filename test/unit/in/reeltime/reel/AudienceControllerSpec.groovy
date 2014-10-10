package in.reeltime.reel

import grails.test.mixin.TestFor
import in.reeltime.common.AbstractControllerSpec
import in.reeltime.exceptions.AuthorizationException
import in.reeltime.exceptions.ReelNotFoundException
import in.reeltime.message.LocalizedMessageService
import in.reeltime.user.User
import spock.lang.Unroll

@TestFor(AudienceController)
class AudienceControllerSpec extends AbstractControllerSpec {

    AudienceService audienceService

    Long reelId

    void setup() {
        audienceService = Mock(AudienceService)
        controller.audienceService = audienceService

        reelId = 1234
        params.reelId = reelId
    }

    @Unroll
    void "reelId cannot be [#invalidId] for action [#actionName]"() {
        given:
        def message = 'TEST'

        and:
        params.reelId = invalidId

        when:
        controller."$actionName"()

        then:
        assertErrorMessageResponse(response, 400, message)

        and:
        1 * localizedMessageService.getMessage('reel.id.required', request.locale) >> message

        where:
        invalidId   |   actionName
        null        |   'listMembers'
        ''          |   'listMembers'
        null        |   'addMember'
        ''          |   'addMember'
        null        |   'removeMember'
        ''          |   'removeMember'
    }

    void "attempt to list audience members of an unknown reel"() {
        given:
        def message = 'unknown reel'

        when:
        controller.listMembers()

        then:
        assertErrorMessageResponse(response, 404, message)

        and:
        1 * audienceService.listMembers(reelId) >> { throw new ReelNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('reel.unknown', request.locale) >> message
    }

    void "attempt to add audience member to an unknown reel"() {
        given:
        def message = 'unknown reel'

        when:
        controller.addMember()

        then:
        assertErrorMessageResponse(response, 404, message)

        and:
        1 * audienceService.addCurrentUserToAudience(reelId) >> { throw new ReelNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('reel.unknown', request.locale) >> message
    }

    void "attempt to remove audience member from an unknown reel"() {
        given:
        def message = 'unknown reel'

        when:
        controller.removeMember()

        then:
        assertErrorMessageResponse(response, 404, message)

        and:
        1 * audienceService.removeCurrentUserFromAudience(reelId) >> { throw new ReelNotFoundException('TEST') }
        1 * localizedMessageService.getMessage('reel.unknown', request.locale) >> message
    }

    void "attempt to remove audience member without proper authorization"() {
        given:
        def message = 'unknown reel'

        when:
        controller.removeMember()

        then:
        assertErrorMessageResponse(response, 403, message)

        and:
        1 * audienceService.removeCurrentUserFromAudience(reelId) >> { throw new AuthorizationException('TEST') }
        1 * localizedMessageService.getMessage('audience.unauthorized', request.locale) >> message
    }

    void "empty audience members list"() {
        when:
        controller.listMembers(reelId)

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 0

        1 * audienceService.listMembers(reelId) >> []
    }

    void "only one member in the audience"() {
        given:
        def member = new User(username: 'member', displayName: 'member display')

        when:
        controller.listMembers(reelId)

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 1

        and:
        json[0].size() == 2
        json[0].username == 'member'
        json[0].display_name == 'member display'

        1 * audienceService.listMembers(reelId) >> [member]
    }

    void "multiple members in the audience"() {
        given:
        def member1 = new User(username: 'member1', displayName: 'member1 display')
        def member2 = new User(username: 'member2', displayName: 'member2 display')

        when:
        controller.listMembers(reelId)

        then:
        assertStatusCodeAndContentType(response, 200)

        and:
        def json = getJsonResponse(response)
        json.size() == 2

        and:
        json[0].size() == 2
        json[0].username == 'member1'
        json[0].display_name == 'member1 display'

        and:
        json[1].size() == 2
        json[1].username == 'member2'
        json[1].display_name == 'member2 display'

        1 * audienceService.listMembers(reelId) >> [member1, member2]
    }

    void "add audience member"() {
        when:
        controller.addMember()

        then:
        response.status == 201
        response.contentLength == 0

        and:
        1 * audienceService.addCurrentUserToAudience(reelId)
    }

    void "remove audience member"() {
        when:
        controller.removeMember()

        then:
        response.status == 200
        response.contentLength == 0

        and:
        1 * audienceService.removeCurrentUserFromAudience(reelId)
    }
}
