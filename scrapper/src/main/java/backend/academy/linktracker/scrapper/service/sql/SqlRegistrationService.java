package backend.academy.linktracker.scrapper.service.sql;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.jdbc.ChatJdbcRepository;
import backend.academy.linktracker.scrapper.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlRegistrationService implements RegistrationService {
    private final ChatJdbcRepository chatRepository;

    @Override
    public void registerChat(Long id) throws ChatAlreadyExistsException, IllegalArgumentException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Невалидный идентификатор чата");
        }
        Chat chat = Chat.builder().chatId(id).build();
        if (chatRepository.existsById(chat.getChatId())) {
            throw new ChatAlreadyExistsException();
        }

        chatRepository.save(chat);
    }

    @Override
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
