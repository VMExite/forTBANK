package backend.academy.linktracker.scrapper.service.crud;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import java.util.List;

public interface ChatsService {
    void registerChat(Long id) throws ChatAlreadyExistsException, IllegalArgumentException;

    void deleteChat(Long id) throws ChatNotExistsException, IllegalArgumentException;

    List<ChatId> getChatIdsByLink(Link link);
}
