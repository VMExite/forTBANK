package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.checker.LinkCheckerHandler;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.parser.LinkParserHandler;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLinkUpdateService implements LinkUpdateService {

    protected final LinkParserHandler parserHandler;
    protected final LinkCheckerHandler checkerHandler;

    protected AbstractLinkUpdateService(LinkParserHandler parserHandler, LinkCheckerHandler checkerHandler) {
        this.parserHandler = parserHandler;
        this.checkerHandler = checkerHandler;
    }

    protected List<LinkUpdateRequest> updateLinksInternal() {
        List<LinkUpdateRequest> updates = new ArrayList<>();
        List<Link> links = findAllLinks();

        for (Link link : links) {
            OffsetDateTime offsetDateTime;
            try {
                offsetDateTime = checkerHandler.check(parserHandler.parse(link.getUrl()));
            } catch (RuntimeException ex) {
                log.warn("link_check_failed url={} error={}", link.getUrl(), ex.toString());
                continue;
            }

            if (offsetDateTime != null && offsetDateTime.isAfter(link.getLastUpdate())) {
                saveLink(link);

                updates.add(new LinkUpdateRequest(
                        link.getLinkId(),
                        link.getUrl(),
                        link.getChats().stream().map(Chat::getChatId).toList()));
            }
        }
        return updates;
    }

    protected abstract List<Link> findAllLinks();

    protected abstract void saveLink(Link link);
}
