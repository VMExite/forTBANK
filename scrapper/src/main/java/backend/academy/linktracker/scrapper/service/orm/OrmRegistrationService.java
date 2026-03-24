package backend.academy.linktracker.scrapper.service.orm;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.jpa.ChatJpaRepository;
import backend.academy.linktracker.scrapper.service.AbstractRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public class OrmRegistrationService extends AbstractRegistrationService {
    private final ChatJpaRepository chatRepository;

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
