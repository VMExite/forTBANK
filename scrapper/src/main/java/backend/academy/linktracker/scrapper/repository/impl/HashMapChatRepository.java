package backend.academy.linktracker.scrapper.repository.impl;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class HashMapChatRepository implements ChatRepository {
    private final HashMap<Long, Chat> storage = new HashMap<>();

    @Override
    public Optional<Chat> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public Chat save(Chat chat) {
        if (storage.containsKey(chat.getId())) {
            storage.remove(chat.getId());
        }
        storage.put(chat.getId(), chat);
        return chat;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}
