package in.reeltime.account

import grails.plugin.springsecurity.annotation.Secured
import grails.util.Environment
import in.reeltime.exceptions.UserNotFoundException

import static javax.servlet.http.HttpServletResponse.*

class DevelopmentOnlyAccountController {

    def developmentOnlyAccountService

    static allowedMethods = [confirmAccountForUser: 'POST', resetPasswordForUser: 'POST']

    private static final List<String> environmentsToAllow = ['test', 'development', 'acceptance']

    def beforeInterceptor = {
        def currentEnvironment = Environment.currentEnvironment.name
        if(!environmentsToAllow.contains(currentEnvironment)) {
            log.warn "Cannot access in environment: $currentEnvironment"
            return false
        }
    }

    @Secured(["#oauth2.isClient() and #oauth2.clientHasRole('ROLE_INTERNAL_CLIENT')"])
    def confirmAccountForUser(String username) {
        developmentOnlyAccountService.confirmAccountForUser(username)
        render(status: SC_OK)
    }

    @Secured(["#oauth2.isClient() and #oauth2.clientHasRole('ROLE_INTERNAL_CLIENT')"])
    def resetPasswordForUser(String username, String new_password) {
        developmentOnlyAccountService.resetPasswordForUser(username, new_password)
        render(status: SC_OK)
    }

    def handleUserNotFoundException(UserNotFoundException e) {
        render(status: SC_NOT_FOUND)
    }
}
