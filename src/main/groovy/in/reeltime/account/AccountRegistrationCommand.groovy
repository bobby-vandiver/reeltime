package in.reeltime.account

import grails.validation.Validateable
import in.reeltime.user.User

class AccountRegistrationCommand implements Validateable {

    def userService

    String display_name
    String email
    String username
    String password
    String client_name

    static constraints = {
        importFrom User, include: ['email', 'username', 'password']

        display_name blank: false, nullable: false, matches: User.DISPLAY_NAME_REGEX
        email validator: emailMustBeAvailable
        username validator: usernameMustBeAvailable
        password minSize: User.PASSWORD_MIN_SIZE
        client_name blank: false, nullable: false
    }

    private static Closure emailMustBeAvailable = { val, obj ->
        if(obj.userService.emailInUse(val)) {
            return 'unavailable'
        }
    }

    private static Closure usernameMustBeAvailable = { val, obj ->
        if(obj.userService.userExists(val)) {
            return 'unavailable'
        }
    }
}
