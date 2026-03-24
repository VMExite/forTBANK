package backend.academy.linktracker.scrapper.service.sql;

import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.jdbc.ChatJdbcRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.LinkJdbcRepository;
import backend.academy.linktracker.scrapper.service.AbstractLinksService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlLinksService extends AbstractLinksService {

    private final ChatJdbcRepository chatRepository;
    private final LinkJdbcRepository linkRepository;

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
