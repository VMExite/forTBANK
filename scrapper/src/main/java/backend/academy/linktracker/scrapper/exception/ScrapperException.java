package backend.academy.linktracker.scrapper.exception;

public class ScrapperException extends RuntimeException {
    public ScrapperException(String message) {
        super(String.format("Ошибка Scrapper: %s", message));
    }

    public ScrapperException(String message, Throwable cause) {
        super(message, cause);
    }
}
