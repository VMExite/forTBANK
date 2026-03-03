package backend.academy.linktracker.scrapper.repository.impl;


import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class HashMapLinksRepository implements LinkRepository {
    private final HashMap<Long, Link> storage = new HashMap<>();

    @Override
    public Optional<Link> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        return storage.values()
            .stream()
            .filter(link -> link.getUrl().equals(url))
            .findFirst();
    }

    @Override
    public List<Link> findAll() {
        return storage.values().stream().toList();
    }

    @Override
    public Link save(Link link) {
        storage.put(link.getId(), link);
        return link;
    }

    @Override
    public void delete(Link link) {
        storage.remove(link.getId());
    }
}
