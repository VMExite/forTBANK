package backend.academy.linktracker.bot.exception;

public class NotificationFailedException extends RuntimeException {
    public NotificationFailedException(String message) {
        super(message);
    }
}
