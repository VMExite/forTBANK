package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinkResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTracked;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class LinksService {

    private final AtomicLong primaryKeyId = new AtomicLong(0);
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;

    public ListLinkResponse getLinks(Long chatId) throws ChatNotExistsException {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(ChatNotExistsException::new);

        List<LinkResponse> responses = chat.getLinks()
            .stream()
            .map(this::mapToResponse)
            .toList();

        return ListLinkResponse.builder()
            .links(responses)
            .size(responses.size())
            .build();
    }

    public LinkResponse createLink(Long chatId, AddLinkRequest request) throws ChatNotExistsException, LinkAlreadyTracked {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(ChatNotExistsException::new);

        boolean alreadyTracked = chat.getLinks()
            .stream()
            .anyMatch(link -> link.getUrl().equals(request.getLink()));
        if (alreadyTracked) {
            throw new LinkAlreadyTracked();
        }

        Link link = Link.builder()
            .id(primaryKeyId.getAndIncrement())
            .url(request.getLink())
            .tags(request.getTags())
            .filters(request.getFilters())
            .build();
        Link savedLink = linkRepository.save(link);

        chat.addLink(savedLink);
        chatRepository.save(chat);

        return mapToResponse(link);
    }

    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) throws ChatNotExistsException, LinkNotFoundException {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(ChatNotExistsException::new);

        Link link = chat.getLinks()
            .stream()
            .filter(l -> l.getUrl().equals(request.getLink()))
            .findFirst()
            .orElseThrow(LinkNotFoundException::new);

        if (link.getChats().isEmpty()) {
            linkRepository.delete(link);
        }

        chat.removeLink(link);
        chatRepository.save(chat);

        return mapToResponse(link);
    }

    private LinkResponse mapToResponse(Link link) {
        return LinkResponse.builder()
            .id(link.getId())
            .url(link.getUrl())
            .tags(link.getTags())
            .filters(link.getFilters())
            .build();
    }
}
