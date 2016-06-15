package in.reeltime.test.client

class EmailFormatter {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator")
    private static final String EMAILS_IN_USE_FILENAME = 'emails-in-use.txt'

    private static File emailsInUseFile

    static {
        emailsInUseFile = new File(EMAILS_IN_USE_FILENAME)

        if (emailsInUseFile.exists()) {
            assert emailsInUseFile.delete() : "Failed to delete $EMAILS_IN_USE_FILENAME"
        }
    }

    static String emailForUsername(String username) {
        String email = 'test-' + username + '@reeltime.in'
        addEmail(email)
        return email
    }

    private static void addEmail(String email) {
        emailsInUseFile.withWriterAppend { writer ->
            writer.append(email)
            writer.append(LINE_SEPARATOR)
        }
    }
}
