package backend.academy.linktracker.scrapper.service.executor;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.parser.LinkParserHandler;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkExecutorHandler {
    private final Set<LinkExecutor> executors;
    private final LinkParserHandler parserHandler;

    public List<LinkUpdateMessage> execute(Link link, List<ChatId> chatIds) {
        ParsedLink parsed = parserHandler.parse(link.getUrl());

        if (parsed == null) {
            return Collections.emptyList();
        }

        List<LinkUpdateMessage> events = executors.stream()
                .filter(e -> e.supports(parsed))
                .findFirst()
                .map(e -> e.execute(link, parsed))
                .orElse(Collections.emptyList());

        return events.stream()
                .flatMap(event -> chatIds.stream()
                        .map(chatId -> new LinkUpdateMessage(
                                chatId.value(),
                                event.linkId(),
                                event.title(),
                                event.username(),
                                event.preview(),
                                event.url(),
                                event.createdAt())))
                .toList();
    }
}
