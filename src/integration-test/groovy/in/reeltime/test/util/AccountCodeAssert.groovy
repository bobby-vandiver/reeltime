package in.reeltime.test.util

import in.reeltime.account.AccountCode
import in.reeltime.account.AccountCodeType
import in.reeltime.user.User

class AccountCodeAssert {

    static void assertResetCodeHasBeenRemoved(User user) {
        assertAccountCodeExistenceForUser(user, AccountCodeType.ResetPassword, false)
    }

    static void assertResetCodeIsAvailable(User user) {
        assertAccountCodeExistenceForUser(user, AccountCodeType.ResetPassword, true)
    }

    static void assertConfirmationCodeHasBeenRemoved(User user) {
        assertAccountCodeExistenceForUser(user, AccountCodeType.AccountConfirmation, false)
    }

    static void assertConfirmationCodeIsAvailable(User user) {
        assertAccountCodeExistenceForUser(user, AccountCodeType.AccountConfirmation, true)
    }

    private static assertAccountCodeExistenceForUser(User user, AccountCodeType type, boolean shouldExist) {
        def accountCodeExists = AccountCode.findByUserAndType(user, type) != null
        assert accountCodeExists == shouldExist
    }
}
