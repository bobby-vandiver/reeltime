package in.reeltime.registration

import in.reeltime.user.User

class AccountConfirmation {

    User user
    String code
    byte[] salt
    Date dateCreated

    static constraints = {
        user nullable: false
        code blank: false, nullable: false
        salt nullable: false, size: 8..8
    }
}
