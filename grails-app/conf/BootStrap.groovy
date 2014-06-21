import in.reeltime.injection.ConfigInjector
import in.reeltime.user.User
import in.reeltime.oauth2.Client

class BootStrap {

    def grailsApplication

    def init = { servletContext ->

        ConfigInjector.injectConfigurableProperties(grailsApplication.config, grailsApplication.mainContext)

        if(!User.findByUsername('bob')) {
            User user = new User(
                    username: 'bob',
                    password: 'pass',
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false
            )
            user.save(flush: true)
            log.info("Added user [${user.username}] with id [${user.id}]")
        }

        if(!Client.findByClientId('test-client')) {
            Client client = new Client(
                    clientName: 'test-client-name',
                    clientId: 'test-client',
                    clientSecret: 'test-secret',
                    authorizedGrantTypes: ['password'],
                    authorities: ['ROLE_NATIVE_CLIENT'],
                    scopes: ['view', 'upload']
            )
            client.save(flush: true)
            log.info("Added client [${client.clientId}] with id [${client.id}]")
        }
    }

    def destroy = {
    }
}
