package backend.academy.linktracker.scrapper.exception;

public class OutboxMappingException extends ScrapperException {
    public OutboxMappingException(String message) {
        super(message);
    }

    public OutboxMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
