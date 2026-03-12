package backend.academy.linktracker.scrapper.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.impl.HashMapLinksRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HashMapLinksRepositoryTest {
    private static final LinkRepository repository = new HashMapLinksRepository();
    private Link link;

    @BeforeEach
    public void init() {
        link = Link.builder()
                .id(1L)
                .url("https://example.com/")
                .tags(List.of("string"))
                .build();
        repository.save(link);
    }

    @AfterEach
    public void clear() {
        repository.deleteById(link.getId());
    }

    @Test
    public void shouldFindByIdTest() {
        Optional<Link> result = repository.findById(link.getId());
        assertTrue(result.isPresent());
        assertEquals(link, result.get());
    }

    @Test
    public void shouldNotFoundByIdTest() {
        Optional<Link> result = repository.findById(link.getId() + 1);
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldRemoveFromRepositoryTest() {
        repository.deleteById(link.getId());
        assertFalse(repository.findById(link.getId()).isPresent());
    }

    @Test
    public void shouldSaveNewModelTest() {
        Link entity = Link.builder()
                .id(link.getId() + 1)
                .url(UUID.randomUUID().toString())
                .tags(link.getTags())
                .build();

        repository.save(entity);
        assertTrue(repository.findById(entity.getId()).isPresent());
    }

    @Test
    public void shouldUpdateModelTest() {
        link.setTags(List.of("string", "string"));

        repository.save(link);

        assertTrue(repository.findById(link.getId()).isPresent());
        assertFalse(repository.findById(link.getId()).get().getTags().isEmpty());
    }
}
