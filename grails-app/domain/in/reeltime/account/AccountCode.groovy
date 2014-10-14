package in.reeltime.account

import in.reeltime.user.User

class AccountCode {

    User user
    String code
    byte[] salt

    AccountCodeType type
    Date dateCreated

    static constraints = {
        user nullable: false
        code blank: false, nullable: false
        salt nullable: false, size: 8..8
        type nullable: false
    }
}
