package in.reeltime.account

import grails.validation.Validateable

@Validateable
class AccountConfirmationCommand {

    String code

    static constraints = {
        code nullable: false, blank: false
    }
}
