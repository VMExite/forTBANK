package backend.academy.linktracker.scrapper.service.orm;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinkResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTracked;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.jpa.ChatJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.LinkJpaRepository;
import backend.academy.linktracker.scrapper.service.LinksService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public class OrmLinksService implements LinksService {

    private final ChatJpaRepository chatRepository;
    private final LinkJpaRepository linkRepository;

    @Override
    @Transactional(readOnly = true)
    public ListLinkResponse getLinks(Long chatId) throws IllegalArgumentException, ChatNotExistsException {
        validateId(chatId);

        Chat chat = chatRepository.findById(chatId).orElseThrow(ChatNotExistsException::new);

        List<LinkResponse> responses =
                chat.getLinks().stream().map(this::mapToResponse).toList();

        return new ListLinkResponse(responses, responses.size());
    }

    @Override
    @Transactional
    public LinkResponse createLink(Long chatId, AddLinkRequest request)
            throws IllegalArgumentException, ChatNotExistsException, LinkAlreadyTracked {
        validateId(chatId);
        if (request == null || request.link() == null || request.link().isBlank()) {
            throw new IllegalArgumentException();
        }

        Chat chat = chatRepository.findById(chatId).orElseThrow(ChatNotExistsException::new);

        boolean alreadyTracked =
                chat.getLinks().stream().anyMatch(link -> link.getUrl().equals(request.link()));
        if (alreadyTracked) {
            throw new LinkAlreadyTracked();
        }

        Link link = Link.builder()
                .url(request.link())
                .tags(request.tags().stream()
                        .map(string -> Tag.builder().name(string).build())
                        .toList())
                .build();
        Link savedLink = linkRepository.save(link);
        chat.getLinks().add(savedLink);
        chatRepository.save(chat);

        return mapToResponse(link);
    }

    @Override
    @Transactional
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request)
            throws IllegalArgumentException, ChatNotExistsException, LinkNotFoundException {
        validateId(chatId);

        if (request == null || request.link() == null || request.link().isBlank()) {
            throw new IllegalArgumentException();
        }

        Chat chat = chatRepository.findById(chatId).orElseThrow(ChatNotExistsException::new);

        Link link = chat.getLinks().stream()
                .filter(l -> l.getUrl().equals(request.link()))
                .findFirst()
                .orElseThrow(LinkNotFoundException::new);

        if (link.getChats().isEmpty()) {
            linkRepository.deleteById(link.getLinkId());
        }

        chat.getLinks().add(link);
        chatRepository.save(chat);

        return mapToResponse(link);
    }

    private LinkResponse mapToResponse(Link link) {
        return new LinkResponse(
                link.getLinkId(),
                link.getUrl(),
                link.getTags().stream().map(Tag::getName).toList());
    }

    private void validateId(Long id) throws IllegalArgumentException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException();
        }
    }
}
