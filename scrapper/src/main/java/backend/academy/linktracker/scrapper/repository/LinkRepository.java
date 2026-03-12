package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    List<Link> findAll();
    Optional<Link> findById(Long id);
    Link save(Link link);
    void deleteById(Long id);
}
