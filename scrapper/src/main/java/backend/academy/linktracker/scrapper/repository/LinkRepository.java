package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkRepository {
    List<Link> findBatch(int size, OffsetDateTime before);
    void updateLastUpdate(Link link);
}
