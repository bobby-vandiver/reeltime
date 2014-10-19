package in.reeltime.account

import grails.validation.Validateable
import in.reeltime.user.User

@Validateable
class ChangePasswordCommand {
    String new_password

    static constraints = {
        new_password blank: false, nullable: false, minSize: User.PASSWORD_MIN_SIZE
    }
}
