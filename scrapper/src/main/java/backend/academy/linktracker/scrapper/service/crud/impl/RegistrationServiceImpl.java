package backend.academy.linktracker.scrapper.service.crud.impl;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.mapper.ChatMapper;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.value.ChatId;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.service.crud.RegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;

    @Override
    public void registerChat(Long id) throws ChatAlreadyExistsException, IllegalArgumentException {
        if (id == null || id < 0) {
            throw new IllegalArgumentException();
        }
        if (chatRepository.findById(new ChatId(id)).isPresent()) {
            throw new ChatAlreadyExistsException();
        }

        Chat chat = chatMapper.fromId(id);
        chatRepository.save(chat);
        log.info("Chat with id {} has been registered successfully", id);
    }

    @Override
    public void deleteChat(Long id) throws ChatNotExistsException, IllegalArgumentException {
        if (id == null || id < 0) {
            throw new IllegalArgumentException();
        }
        if (chatRepository.findById(new ChatId(id)).isEmpty()) {
            throw new ChatNotExistsException();
        }

        chatRepository.deleteById(new ChatId(id));
        log.info("Chat with id {} has been deleted successfully", id);
    }
}
