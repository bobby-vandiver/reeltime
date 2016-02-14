package in.reeltime.test.util

import in.reeltime.test.client.EmailFormatter
import in.reeltime.mail.local.FileSystemMailService

class EmailReader {

    private static final String ACCOUNT_CONFIRMATION_EMAIL_SUBJECT = 'Please Verify Your ReelTime Account'

    private static final String ACCOUNT_CONFIRMATION_EMAIL_REGEX =
            /Hello (\w+), please enter the following code on your registered device: ([a-zA-z0-9]{43})/

    private static final String RESET_PASSWORD_EMAIL_SUBJECT = 'ReelTime Password Reset'

    private static final String RESET_PASSWORD_EMAIL_REGEX =
            /Hello (\w+), please enter the following code when prompted to reset your password: ([a-zA-z0-9]{43})/

    String accountConfirmationCode(String username) {
        return accountCodeFromEmail(username, ACCOUNT_CONFIRMATION_EMAIL_SUBJECT, ACCOUNT_CONFIRMATION_EMAIL_REGEX)
    }

    String resetPasswordCode(String username) {
        return accountCodeFromEmail(username, RESET_PASSWORD_EMAIL_SUBJECT, RESET_PASSWORD_EMAIL_REGEX)
    }

    private String accountCodeFromEmail(String username, String subject, String regex) {
        def email = getEmail(username, subject)

        def matcher = (email.text =~ regex)
        matcher.matches()

        assert matcher[0][1] == username
        return matcher[0][2] as String
    }

    private File getEmail(String username, String subject) {
        def emailAddress = EmailFormatter.emailForUsername(username)

        def directory = FileSystemMailService.TEMP_DIR
        def filename = FileSystemMailService.getFilename(emailAddress, 'noreply@reeltime.in', subject)

        return new File(directory, filename)
    }
}
