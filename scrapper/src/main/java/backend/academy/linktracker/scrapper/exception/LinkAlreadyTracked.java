package backend.academy.linktracker.scrapper.exception;

public class LinkAlreadyTracked extends ScrapperException {
    public LinkAlreadyTracked() {
        super("ссылка уже отслеживается");
    }
}
