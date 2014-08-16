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

        if(!obj.userAuthenticationService.authenticate(username, password)) {
            return 'unauthenticated'
        }
    }
}
