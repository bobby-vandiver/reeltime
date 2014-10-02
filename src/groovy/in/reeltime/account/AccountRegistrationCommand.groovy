package in.reeltime.account

import grails.validation.Validateable
import in.reeltime.user.User

@Validateable
class AccountRegistrationCommand {

    def userService

    String display_name
    String email
    String username
    String password
    String client_name

    static constraints = {
        importFrom User, include: ['email', 'username', 'password']

        display_name blank: false, nullable: false, matches: User.DISPLAY_NAME_REGEX
        username validator: usernameMustBeAvailable
        password minSize: 6
        client_name blank: false, nullable: false
    }

    private static Closure usernameMustBeAvailable = { val, obj ->
        if(obj.userService.userExists(val)) {
            return 'unavailable'
        }
    }
}
