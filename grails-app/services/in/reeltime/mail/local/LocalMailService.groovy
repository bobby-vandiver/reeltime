package in.reeltime.mail.local

import in.reeltime.mail.Email
import in.reeltime.mail.MailService

class LocalMailService implements MailService {

    InMemoryMailService inMemoryMailService
    FileSystemMailService fileSystemMailService

    @Override
    void sendMail(Email email) {
        inMemoryMailService.sendMail(email)
        fileSystemMailService.sendMail(email)
    }
}
