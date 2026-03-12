package backend.academy.linktracker.scrapper.exception;

public class ChatNotExistsException extends ScrapperException {
    public ChatNotExistsException() {
        super("чат не существует");
    }
}
