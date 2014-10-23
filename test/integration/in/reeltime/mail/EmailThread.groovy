package in.reeltime.mail

class EmailThread extends Thread {

    EmailManager emailManager

    Queue<Email> queue
    String prefix

    @Override
    void run() {
        emailManager.sendMail(prefix + '-to', prefix + '-from', prefix + '-subject', prefix + '-body')
        queue = emailManager.emails.get()
    }
}