package in.reeltime.mail

import org.springframework.transaction.support.TransactionSynchronizationAdapter

class EmailManager extends TransactionSynchronizationAdapter {

    MailService mailService

    private static ThreadLocal<Queue<Email>> emails = new ThreadLocal<Queue>() {
        @Override
        protected Queue<Email> initialValue() {
            return new LinkedList<Email>()
        }
    }

    void sendMail(String to, String from, String subject, String body) {
        def email = new Email(to: to, from: from, subject: subject, body: body)
        def queue = emails.get()
        queue.add(email)
    }

    @Override
    void afterCompletion(int status) {
        super.afterCompletion(status)
    }
}
