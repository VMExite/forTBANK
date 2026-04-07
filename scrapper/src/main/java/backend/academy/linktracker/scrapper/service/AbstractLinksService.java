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
import backend.academy.linktracker.scrapper.model.Tag;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractLinksService implements LinksService {

    @Override
    public ListLinkResponse getLinks(Long chatId) throws IllegalArgumentException, ChatNotExistsException {
        validateId(chatId);

        Chat chat = getChatOrThrow(chatId);
        List<LinkResponse> responses =
                chat.getLinks().stream().map(this::mapToResponse).toList();
        return new ListLinkResponse(responses, responses.size());
    }

    @Override
    public LinkResponse createLink(Long chatId, AddLinkRequest request)
            throws IllegalArgumentException, ChatNotExistsException, LinkAlreadyTracked {
        validateId(chatId);

        if (request == null || request.link() == null || request.link().isBlank()) {
            throw new IllegalArgumentException();
        }

        Chat chat = getChatOrThrow(chatId);

        boolean alreadyTracked =
                chat.getLinks().stream().anyMatch(link -> link.getUrl().equals(request.link()));
        if (alreadyTracked) {
            throw new LinkAlreadyTracked();
        }

        Link link = Link.builder()
                .url(request.link())
                .tags(request.tags().stream()
                        .map(string -> Tag.builder().name(string).build())
                        .collect(Collectors.toSet()))
                .build();
        Link savedLink = saveLink(link);
        chat.getLinks().add(savedLink);
        saveChat(chat);

        return mapToResponse(link);
    }

    @Override
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request)
            throws IllegalArgumentException, ChatNotExistsException, LinkNotFoundException {
        validateId(chatId);

        if (request == null || request.link() == null || request.link().isBlank()) {
            throw new IllegalArgumentException();
        }

        Chat chat = getChatOrThrow(chatId);

        Link link = chat.getLinks().stream()
                .filter(l -> l.getUrl().equals(request.link()))
                .findFirst()
                .orElseThrow(LinkNotFoundException::new);

        if (link.getChats().isEmpty()) {
            deleteLinkById(link.getLinkId());
        }

        chat.getLinks().remove(link);
        saveChat(chat);

        return mapToResponse(link);
    }

    protected abstract Chat getChatOrThrow(Long chatId) throws ChatNotExistsException;

    protected abstract Link saveLink(Link link);

    protected abstract void deleteLinkById(Long linkId);

    protected abstract void saveChat(Chat chat);

    protected LinkResponse mapToResponse(Link link) {
        return new LinkResponse(
                link.getLinkId(),
                link.getUrl(),
                link.getTags().stream().map(Tag::getName).toList());
    }

    protected void validateId(Long id) throws IllegalArgumentException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException();
        }
    }
}
