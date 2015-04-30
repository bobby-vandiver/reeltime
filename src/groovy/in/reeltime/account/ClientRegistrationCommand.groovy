package in.reeltime.account

import grails.validation.Validateable

@Validateable
class ClientRegistrationCommand {

    def authenticationService
    def userService

    String username
    String password
    String client_name

    static constraints = {
        username blank: false, nullable: false, validator: userAuthenticationValidator
        password blank: false, nullable: false
        client_name blank: false, nullable: false, validator: clientNameAvailabilityValidator
    }

    private static Closure userAuthenticationValidator = { val, obj ->
        def username = obj.username
        def password = obj.password

        // Let the blank and nullable constraints handle these scenarios
        // so the messages returned are accurate for the error encountered
        if(!username || !password) {
            return true
        }
        else if(!authenticateUser(obj)) {
            return 'unauthenticated'
        }
    }

    private static Closure clientNameAvailabilityValidator = { val, obj ->
        def username = obj.username
        def password = obj.password
        def clientName = obj.client_name

        // If the credentials are invalid, there's no reason to validate
        // the client name since we're not dealing with a known user.
        if (!authenticateUser(obj)) {
            return true
        }
        // Similar to above, we will let the built-in validators
        // handle blank and null values
        else if(!username || !password || !clientName) {
            return true
        }
        else if(!obj.userService.isClientNameAvailable(username, clientName)) {
            return 'unavailable'
        }
    }

    private static boolean authenticateUser(obj) {
        def username = obj.username
        def password = obj.password

        return obj.authenticationService.authenticateUser(username, password)
    }
}
