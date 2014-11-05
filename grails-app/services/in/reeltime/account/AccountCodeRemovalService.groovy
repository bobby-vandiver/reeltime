package in.reeltime.account

import in.reeltime.user.User

class AccountCodeRemovalService {

    void removeConfirmationCodesForUser(User user) {
        def confirmationCodes = AccountCode.findAllByUser(user)
        confirmationCodes.each { code ->
            log.debug "Deleting account confirmation code [${code.id}]"
            code.delete()
        }
    }
}
