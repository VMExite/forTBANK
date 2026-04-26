package backend.academy.linktracker.bot.exception;

public class NotRetryableException extends RuntimeException {
    public NotRetryableException(String message) {
        super(message);
    }
}
