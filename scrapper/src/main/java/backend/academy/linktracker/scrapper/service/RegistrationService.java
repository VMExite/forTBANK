package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final ChatRepository chatRepository;

    public void registerChat(Long id) throws ChatAlreadyExistsException, IllegalArgumentException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Невалидный идентификатор чата");
        }
        Chat chat = Chat.builder().id(id).build();
        if (chatRepository.existsById(chat.getId())) {
            throw new ChatAlreadyExistsException();
        }

        chatRepository.save(chat);
    }

    public void deleteChat(Long id) throws ChatNotExistsException, IllegalArgumentException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Невалидный идентификатор чата");
        }
        if (!chatRepository.existsById(id)) {
            throw new ChatNotExistsException();
        }
        chatRepository.deleteById(id);
    }
}
