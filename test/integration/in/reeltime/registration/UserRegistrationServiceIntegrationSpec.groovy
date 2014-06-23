package in.reeltime.registration

import grails.test.spock.IntegrationSpec
import in.reeltime.oauth2.Client
import in.reeltime.user.User
import in.reeltime.exceptions.RegistrationException

class UserRegistrationServiceIntegrationSpec extends IntegrationSpec {

    def userRegistrationService

    void "register new user"() {
        given:
        def username = 'foo'
        def password = 'bar'

        and:
        def client = new Client(clientName: 'test-name', clientId: 'test-id').save()

        when:
        def user = userRegistrationService.register(username, password, client)

        then:
        user.id > 0

        and:
        user.username == username
        user.password != password

        and:
        user.clients.size() == 1
        user.clients[0] == client
    }
}
