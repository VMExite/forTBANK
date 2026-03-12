package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Chat;
import java.util.Optional;

public interface ChatRepository {
    Optional<Chat> findById(Long id);

    boolean existsById(Long id);

    Chat save(Chat chat);

    void deleteById(Long id);
}
