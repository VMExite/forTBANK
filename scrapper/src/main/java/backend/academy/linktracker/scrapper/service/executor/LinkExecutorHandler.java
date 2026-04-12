package backend.academy.linktracker.scrapper.service.executor;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.parser.LinkParserHandler;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkExecutorHandler {
    private final Set<LinkExecutor> executors;
    private final LinkParserHandler parserHandler;
    private final ChatRepository chatRepository;

    public List<LinkUpdateMessage> execute(Link link) {

        ParsedLink parsed = parserHandler.parse(link.getUrl());

        if (parsed == null) {
            return Collections.emptyList();
        }

        List<LinkUpdateMessage> events = executors.stream()
                .filter(e -> e.supports(parsed))
                .findFirst()
                .map(e -> e.execute(link, parsed))
                .orElse(Collections.emptyList());

        List<ChatId> chatIds = chatRepository.findChatIdByListId(link.getLinkId());

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
