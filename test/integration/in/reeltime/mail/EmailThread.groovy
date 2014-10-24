package in.reeltime.mail

import org.springframework.transaction.support.TransactionSynchronizationManager

class EmailThread extends Thread {

    EmailManager emailManager

    Queue<Email> queue
    String prefix

    @Override
    void run() {
        boolean activatedSynchronization = false

        if(!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization()
            activatedSynchronization = true
        }

        emailManager.sendMail(prefix + '-to', prefix + '-from', prefix + '-subject', prefix + '-body')
        queue = emailManager.emails.get()

        if(activatedSynchronization) {
            TransactionSynchronizationManager.clearSynchronization()
        }
    }
}