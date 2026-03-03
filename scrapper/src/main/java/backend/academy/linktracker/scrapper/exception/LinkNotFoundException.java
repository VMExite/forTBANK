package backend.academy.linktracker.scrapper.exception;

public class LinkNotFoundException extends ScrapperException {
    public LinkNotFoundException() {
        super("ссылка не найдена");
    }
}
