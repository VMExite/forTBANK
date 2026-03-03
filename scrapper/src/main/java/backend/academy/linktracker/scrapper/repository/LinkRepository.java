package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    Optional<Link> findById(Long id);
    Optional<Link> findByUrl(String url);
    List<Link> findAll();
    Link save(Link link);
    void delete(Link link);
}
