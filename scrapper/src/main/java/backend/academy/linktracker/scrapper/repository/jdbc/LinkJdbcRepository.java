package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.Link;
import java.util.List;

public interface LinkJdbcRepository extends JdbcRepository<Link, Long> {
    List<Link> findLinkByUrl(String url);
}
