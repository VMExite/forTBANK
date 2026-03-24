package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotExistsException;

public interface RegistrationService {
    void registerChat(Long id) throws ChatAlreadyExistsException, IllegalArgumentException;

    void deleteChat(Long id) throws ChatNotExistsException, IllegalArgumentException;
}
