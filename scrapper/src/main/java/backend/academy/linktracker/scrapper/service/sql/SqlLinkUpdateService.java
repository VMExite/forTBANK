package backend.academy.linktracker.scrapper.service.sql;

import backend.academy.linktracker.scrapper.checker.LinkCheckerHandler;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.parser.LinkParserHandler;
import backend.academy.linktracker.scrapper.repository.jdbc.LinkJdbcRepository;
import backend.academy.linktracker.scrapper.service.AbstractLinkUpdateService;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlLinkUpdateService extends AbstractLinkUpdateService {
    private final LinkJdbcRepository repository;

    public SqlLinkUpdateService(
            LinkJdbcRepository repository, LinkParserHandler parserHandler, LinkCheckerHandler checkerHandler) {
        super(parserHandler, checkerHandler);
        this.repository = repository;
    }

    @Override
    public List<LinkUpdateRequest> updateLinks() {
        return updateLinksInternal();
    }

    @Override
    protected List<Link> findAllLinks() {
        return repository.findAll();
    }

    @Override
    protected void saveLink(Link link) {
        repository.save(link);
    }
}
