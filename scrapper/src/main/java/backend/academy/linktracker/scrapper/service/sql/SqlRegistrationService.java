package backend.academy.linktracker.scrapper.service.sql;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.jdbc.ChatJdbcRepository;
import backend.academy.linktracker.scrapper.service.AbstractRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlRegistrationService extends AbstractRegistrationService {
    private final ChatJdbcRepository chatRepository;

    @Override
    protected boolean chatExists(Long id) {
        return chatRepository.existsById(id);
    }

    @Override
    protected void saveChat(Chat chat) {
        chatRepository.save(chat);
    }

    @Override
    protected void deleteChatById(Long id) {
        chatRepository.deleteById(id);
    }
}
