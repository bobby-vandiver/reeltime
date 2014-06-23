package in.reeltime.registration

import grails.validation.Validateable

@Validateable
class RegistrationCommand {

    def userService

    String username
    String password
    String client_name

    static constraints = {
        username blank: false, nullable: false, validator: usernameMustBeAvailable
        password blank: false, nullable: false
        client_name blank: false, nullable: false
    }

    private static Closure usernameMustBeAvailable = { val, obj ->
        if(obj.userService.userExists(val)) {
            return 'unavailable'
        }
    }
}
