package backend.academy.linktracker.scrapper.exception;

public final class LinkNotFoundException extends ScrapperException {
    public LinkNotFoundException() {
        super("ссылка не найдена");
    }
}
