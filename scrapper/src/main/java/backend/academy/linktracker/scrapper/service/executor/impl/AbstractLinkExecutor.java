package backend.academy.linktracker.scrapper.service.executor.impl;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.service.executor.LinkExecutor;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

abstract class AbstractLinkExecutor implements LinkExecutor {
    @Override
    public List<LinkUpdateMessage> execute(Link link, ParsedLink parsed) {
        if (!supports(parsed)) {
            return Collections.emptyList();
        }
        return findNewEvents(fetchEvents(parsed, link), link.getLastUpdate());
    }

    protected List<LinkUpdateMessage> findNewEvents(List<LinkUpdateMessage> events, OffsetDateTime lastUpdate) {
        return events.stream().filter(e -> e.createdAt().isAfter(lastUpdate)).toList();
    }

    protected String preview(String body) {
        if (body == null) return "";
        return body.length() <= 200 ? body : body.substring(0, 200);
    }

    protected abstract List<LinkUpdateMessage> fetchEvents(ParsedLink parsedLink, Link link);
}
