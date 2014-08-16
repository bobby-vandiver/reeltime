package in.reeltime.account

import grails.validation.Validateable

@Validateable
class ClientRegistrationCommand {

    def userAuthenticationService

    String username
    String password
    String client_name

    static constraints = {
        username blank: false, nullable: false, validator: userAuthenticationValidator
        password blank: false, nullable: false
        client_name blank: false, nullable: false
    }

    private static Closure userAuthenticationValidator = { val, obj ->
        def username = obj.username
        def password = obj.password

        // Let the blank and nullable constraints handle these scenarios
        // so the messages returned are accurate for the error encountered
        if(!username || !password) {
            return true
        }
        else if(!obj.userAuthenticationService.authenticate(username, password)) {
            return 'unauthenticated'
        }
    }
}
