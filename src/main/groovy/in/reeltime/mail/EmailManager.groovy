package in.reeltime.mail

import groovy.util.logging.Slf4j
import in.reeltime.exceptions.MailServerNotFoundException
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager

@Slf4j
class EmailManager extends TransactionSynchronizationAdapter {

    MailService mailService
    MailServerExistenceService mailServerExistenceService

    private static ThreadLocal<Queue<Email>> emails = new ThreadLocal<Queue>() {
        @Override
        protected Queue<Email> initialValue() {
            return new LinkedList<Email>()
        }
    }

    void sendMail(String to, String from, String subject, String body) {
        verifyMailServerExistsForEmailAddress(to)

        if(!TransactionSynchronizationManager.isSynchronizationActive()) {
            return
        }
        TransactionSynchronizationManager.registerSynchronization(this)

        def email = new Email(to: to, from: from, subject: subject, body: body)
        def queue = emails.get()
        queue.add(email)
    }

    private void verifyMailServerExistsForEmailAddress(String emailAddress) {
        def host = EmailUtils.getHostFromEmailAddress(emailAddress)

        if (!mailServerExistenceService.exists(host)) {
            throw new MailServerNotFoundException("Mail server [$host] not found")
        }
    }

    @Override
    void afterCompletion(int status) {
        log.debug "Transaction status: $status"
        if(status == STATUS_COMMITTED) {
            sendAllQueuedEmails()
        }
        emails.remove()
    }

    private void sendAllQueuedEmails() {
        def queue = emails.get()
        while(!queue.empty) {
            def email = queue.remove()
            mailService.sendMail(email)
        }
    }
}
