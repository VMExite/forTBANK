package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import java.util.Optional;

public interface ChatRepository {
    Optional<Chat> findById(ChatId id);
    Chat save(Chat chat);
    void deleteById(ChatId id);
}
