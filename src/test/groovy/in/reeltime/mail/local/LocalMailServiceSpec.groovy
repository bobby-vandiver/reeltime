package in.reeltime.mail.local

import grails.test.mixin.TestFor
import in.reeltime.mail.Email
import spock.lang.Specification

@TestFor(LocalMailService)
class LocalMailServiceSpec extends Specification {

    InMemoryMailService inMemoryMailService
    FileSystemMailService fileSystemMailService

    void setup() {
        inMemoryMailService = Mock(InMemoryMailService)
        fileSystemMailService = Mock(FileSystemMailService)

        service.inMemoryMailService = inMemoryMailService
        service.fileSystemMailService = fileSystemMailService
    }

    void "delegate to in memory and file system mail services"() {
        given:
        def email = new Email()

        when:
        service.sendMail(email)

        then:
        1 * inMemoryMailService.sendMail(email)
        1 * fileSystemMailService.sendMail(email)
    }
}
