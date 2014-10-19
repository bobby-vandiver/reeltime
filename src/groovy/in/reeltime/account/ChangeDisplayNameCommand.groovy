package in.reeltime.account

import grails.validation.Validateable
import in.reeltime.user.User

@Validateable
class ChangeDisplayNameCommand {
    String new_display_name

    static constraints = {
        new_display_name blank: false, nullable: false, matches: User.DISPLAY_NAME_REGEX
    }
}
