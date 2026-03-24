package backend.academy.linktracker.scrapper.exception;

public final class ChatAlreadyExistsException extends ScrapperException {
    public ChatAlreadyExistsException() {
        super("чат уже существует");
    }
}
