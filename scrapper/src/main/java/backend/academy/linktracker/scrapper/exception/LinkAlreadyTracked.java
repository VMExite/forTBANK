package backend.academy.linktracker.scrapper.exception;

public final class LinkAlreadyTracked extends ScrapperException {
    public LinkAlreadyTracked() {
        super("ссылка уже отслеживается");
    }
}
