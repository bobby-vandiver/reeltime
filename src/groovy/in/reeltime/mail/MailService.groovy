package in.reeltime.mail

interface MailService {

    /**
     * Creates and sends an email to the specified email address.
     *
     * @param to The destination email address. This should appear as the to address on the delivered email.
     * @param from The source email address. This should appear as the from address on the delivered email.
     * @param subject The subject of the email.
     * @param body The content of the email.
     */
    void sendMail(String to, String from, String subject, String body)
}
