package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.model.Chat;

public abstract class AbstractRegistrationService implements RegistrationService {

    @Override
    public void registerChat(Long id) throws ChatAlreadyExistsException, IllegalArgumentException {
        validateId(id);
        Chat chat = Chat.builder().chatId(id).build();
        if (chatExists(chat.getChatId())) {
            throw new ChatAlreadyExistsException();
        }

        saveChat(chat);
    }

    @Override
    public void deleteChat(Long id) throws ChatNotExistsException, IllegalArgumentException {
        validateId(id);
        if (!chatExists(id)) {
            throw new ChatNotExistsException();
        }
        deleteChatById(id);
    }

    protected abstract boolean chatExists(Long id);

    protected abstract void saveChat(Chat chat);

    protected abstract void deleteChatById(Long id);

    protected void validateId(Long id) throws IllegalArgumentException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Невалидный идентификатор чата");
        }
    }
}
