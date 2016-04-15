package in.reeltime.mail.local

import grails.transaction.Transactional
import in.reeltime.mail.Email
import in.reeltime.mail.MailService

@Transactional
class LocalMailService implements MailService {

    InMemoryMailService inMemoryMailService
    FileSystemMailService fileSystemMailService

    @Override
    void sendMail(Email email) {
        inMemoryMailService.sendMail(email)
        fileSystemMailService.sendMail(email)
    }
}
