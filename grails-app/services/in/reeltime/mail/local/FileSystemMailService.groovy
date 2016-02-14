package in.reeltime.mail.local

import in.reeltime.mail.Email
import in.reeltime.mail.MailService
import in.reeltime.storage.local.LocalFileSystemStorageService

import java.nio.charset.StandardCharsets

class FileSystemMailService implements MailService {

    Set<File> sentMessages = [].asSynchronized()
    LocalFileSystemStorageService localFileSystemStorageService

    static final String TEMP_DIR = System.getProperty("java.io.tmpdir")

    @Override
    void sendMail(Email email) {
        def to = email.to
        def from = email.from
        def subject = email.subject
        def body = email.body

        def filename = getFilename(to, from, subject)

        def inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))
        localFileSystemStorageService.store(inputStream, TEMP_DIR, filename)

        sentMessages << new File(TEMP_DIR, filename)
    }

    String getMailBody(String to, String from, String subject) {
        def filename = getFilename(to, from, subject)
        localFileSystemStorageService.load(TEMP_DIR, filename).text
    }

    static String getFilename(String to, String from, String subject) {
        return "to-$to--from-$from--subject-$subject"
    }
}
