package in.reeltime.exceptions;

public class MailServerNotFoundException extends RuntimeException {

    public MailServerNotFoundException(String message) {
        super(message);
    }
}
