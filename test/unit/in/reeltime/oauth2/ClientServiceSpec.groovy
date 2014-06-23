package in.reeltime.oauth2

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import in.reeltime.exceptions.RegistrationException
import spock.lang.Unroll
import in.reeltime.oauth2.Client

@TestFor(ClientService)
@Mock([Client])
class ClientServiceSpec extends Specification {

    void "generate random client id"() {
        expect:
        service.generateClientId().length() > 0
    }

    @Unroll
    void "generated client id must be unique -- duplicate for first [#repeatCount] tries"() {
        given:
        def existingId = 'this-is-a-test'
        createAndSaveClient(existingId)

        and:
        int count = 0
        UUID.metaClass.'static'.randomUUID = {
            if(++count >= repeatCount) {
                UUID.metaClass = null
            }
            return existingId
        }

        when:
        def generatedId = service.generateClientId()

        then:
        generatedId.length() > 0
        generatedId != existingId

        cleanup:
        UUID.metaClass = null

        where:
        repeatCount << [1, 2, 3, 4, 5]
    }

    void "throw exception if unable to generate unique client id after 5 tries"() {
        given:
        def existingId = 'this-is-a-test'
        createAndSaveClient(existingId)

        and:
        UUID.metaClass.'static'.randomUUID = {
            return existingId
        }

        when:
        service.generateClientId()

        then:
        def e = thrown(RegistrationException)
        e.message == 'Cannot generate unique client id'

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
