package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.checker.LinkCheckerHandler;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.parser.LinkParserHandler;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdateService {
    private final LinkRepository repository;
    private final LinkParserHandler parserHandler;
    private final LinkCheckerHandler checkerHandler;

    public List<LinkUpdateRequest> updateLinks() {
        List<LinkUpdateRequest> updates = new ArrayList<>();
        List<Link> links = repository.findAll();

        for (Link link : links) {
            OffsetDateTime offsetDateTime;
            try {
                offsetDateTime = checkerHandler.check(parserHandler.parse(link.getUrl()));
            } catch (RuntimeException ex) {
                log.warn("link_check_failed url={} error={}", link.getUrl(), ex.toString());
                continue;
            }

            if (offsetDateTime != null && offsetDateTime.isAfter(link.getLastUpdate())) {
                link.setLastUpdate(offsetDateTime);
                repository.save(link);

                updates.add(new LinkUpdateRequest(
                        link.getId(),
                        link.getUrl(),
                        link.getChats().stream().map(Chat::getId).toList()));
            }
        }
        return updates;
    }
}
