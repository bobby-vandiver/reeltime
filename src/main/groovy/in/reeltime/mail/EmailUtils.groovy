package in.reeltime.mail

class EmailUtils {

    private static final int NOT_FOUND = -1

    static String getHostFromEmailAddress(String emailAddress) {
        int idx = emailAddress?.indexOf('@') ?: NOT_FOUND
        return idx != NOT_FOUND ? emailAddress.substring(idx + 1) : emailAddress
    }
}
