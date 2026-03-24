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
import backend.academy.linktracker.scrapper.repository.jpa.ChatJpaRepository;
import backend.academy.linktracker.scrapper.repository.jpa.LinkJpaRepository;
import backend.academy.linktracker.scrapper.service.AbstractLinksService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public class OrmLinksService extends AbstractLinksService {

    private final ChatJpaRepository chatRepository;
    private final LinkJpaRepository linkRepository;

    @Override
    @Transactional(readOnly = true)
    public ListLinkResponse getLinks(Long chatId) throws IllegalArgumentException, ChatNotExistsException {
        return super.getLinks(chatId);
    }

    @Override
    @Transactional
    public LinkResponse createLink(Long chatId, AddLinkRequest request)
            throws IllegalArgumentException, ChatNotExistsException, LinkAlreadyTracked {
        return super.createLink(chatId, request);
    }

    @Override
    @Transactional
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request)
            throws IllegalArgumentException, ChatNotExistsException, LinkNotFoundException {
        return super.removeLink(chatId, request);
    }

    @Override
    protected Chat getChatOrThrow(Long chatId) throws ChatNotExistsException {
        return chatRepository.findById(chatId).orElseThrow(ChatNotExistsException::new);
    }

    @Override
    protected Link saveLink(Link link) {
        return linkRepository.save(link);
    }

    @Override
    protected void deleteLinkById(Long linkId) {
        linkRepository.deleteById(linkId);
    }

    @Override
    protected void saveChat(Chat chat) {
        chatRepository.save(chat);
    }
}
