package backend.academy.linktracker.scrapper.exception;

public final class ChatNotExistsException extends ScrapperException {
    public ChatNotExistsException() {
        super("чат не существует");
    }
}
