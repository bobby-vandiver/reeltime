package in.reeltime.registration

import in.reeltime.user.User

class AccountVerification {

    User user
    String code
    Date dateCreated

    static constraints = {
        user nullable: false
        code blank: false, nullable: false
    }
}
