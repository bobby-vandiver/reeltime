package in.reeltime.mail.local

import in.reeltime.mail.MailServerExistenceService

class LocalMailServerExistenceService implements MailServerExistenceService {

    @Override
    boolean exists(String host) {
        return true
    }
}
