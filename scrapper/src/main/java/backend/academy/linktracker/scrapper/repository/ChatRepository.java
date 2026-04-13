package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.model.value.LinkId;
import java.util.List;
import java.util.Optional;

public interface ChatRepository {
    Optional<Chat> findById(ChatId id);

    List<ChatId> findChatIdByLinkId(LinkId listId);

    Chat save(Chat chat);

    void deleteById(ChatId id);
}
