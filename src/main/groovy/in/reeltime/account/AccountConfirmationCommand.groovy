package in.reeltime.account

import grails.validation.Validateable

class AccountConfirmationCommand implements Validateable {

    String code

    static constraints = {
        code nullable: false, blank: false
    }
}
