package in.reeltime.oauth2

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ClientRegistrationService)
@Mock([Client])
class ClientRegistrationSpec extends Specification {

    void "generate random client id"() {
        expect:
        service.generateClientId().length() > 0
    }

    void "generated client id must be unique"() {
        given:
        def existingId = 'this-is-a-test'
        createAndSaveClient(existingId)

        and:
        UUID.metaClass.'static'.randomUUID = {
            UUID.metaClass = null
            return existingId
        }

        when:
        def generatedId = service.generateClientId()

        then:
        generatedId.length() > 0
        generatedId != existingId

        cleanup:
        UUID.metaClass = null
    }

    private void createAndSaveClient(String clientId) {
        def client = new Client(clientId: clientId)
        client.springSecurityService = Stub(SpringSecurityService)
        client.save(validate: false)
        assert client.id
    }

    void "generate secure random client secret"() {
        expect:
        service.generateClientSecret().length() == 42
    }

    void "generate different passwords for subsequent executions"() {
        when:
        def firstSecret = service.generateClientSecret()
        def secondSecret = service.generateClientSecret()

        then:
        firstSecret != secondSecret
    }
}
