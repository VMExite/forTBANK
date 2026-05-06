package backend.academy.linktracker.bot.exception;

public class RetryableException extends RuntimeException {
    public RetryableException() {}

    public RetryableException(String message) {
        super(message);
    }
}
