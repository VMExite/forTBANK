package backend.academy.linktracker.scrapper.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.impl.HashMapChatRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HashMapChatRepositoryTest {
    private static final ChatRepository repository = new HashMapChatRepository();
    private Chat chat;

    @BeforeEach
    public void init() {
        chat = new Chat(1L);
        repository.save(chat);
    }

    @AfterEach
    public void clear() {
        repository.deleteById(chat.getId());
    }

    @Test
    public void shouldFindByIdTest() {
        Optional<Chat> result = repository.findById(chat.getId());
        assertTrue(result.isPresent());
        assertEquals(chat, result.get());
    }

    @Test
    public void shouldNotFoundByIdTest() {
        Optional<Chat> result = repository.findById(chat.getId() + 1);
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldExistInRepositoryTest() {
        assertTrue(repository.existsById(chat.getId()));
    }

    @Test
    public void shouldNotExistsRepositoryTest() {
        assertFalse(repository.existsById(chat.getId() + 1));
    }

    @Test
    public void shouldRemoveFromRepositoryTest() {
        repository.deleteById(chat.getId());
        assertFalse(repository.existsById(chat.getId()));
    }

    @Test
    public void shouldSaveNewModelTest() {
        Chat entity = new Chat(chat.getId() + 1);

        repository.save(entity);
        assertTrue(repository.existsById(entity.getId()));
    }

    @Test
    public void shouldUpdateModelTest() {
        Link link = new Link();
        chat.addLink(link);

        repository.save(chat);

        assertTrue(repository.existsById(chat.getId()));
        assertTrue(repository.findById(chat.getId()).isPresent());
        assertFalse(repository.findById(chat.getId()).get().getLinks().isEmpty());
    }
}
