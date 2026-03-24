package backend.academy.linktracker.scrapper.service.sql;

import backend.academy.linktracker.scrapper.checker.LinkCheckerHandler;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.parser.LinkParserHandler;
import backend.academy.linktracker.scrapper.repository.jdbc.LinkJdbcRepository;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlLinkUpdateService implements LinkUpdateService {
    private final LinkJdbcRepository repository;
    private final LinkParserHandler parserHandler;
    private final LinkCheckerHandler checkerHandler;

    @Override
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
                repository.save(link);

                updates.add(new LinkUpdateRequest(
                        link.getLinkId(),
                        link.getUrl(),
                        link.getChats().stream().map(Chat::getChatId).toList()));
            }
        }
        return updates;
    }
}
