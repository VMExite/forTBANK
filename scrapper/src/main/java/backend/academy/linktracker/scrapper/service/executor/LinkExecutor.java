package backend.academy.linktracker.scrapper.service.executor;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.model.Link;
import java.util.List;

public interface LinkExecutor {
    boolean supports(ParsedLink link);

    List<LinkUpdateMessage> execute(Link link, ParsedLink parsedLink);
}
