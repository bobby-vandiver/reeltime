package in.reeltime.mail.local

import grails.transaction.Transactional
import in.reeltime.mail.MailServerExistenceService

@Transactional
class LocalMailServerExistenceService implements MailServerExistenceService {

    @Override
    boolean exists(String host) {
        return true
    }
}
