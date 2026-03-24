package backend.academy.linktracker.scrapper.service.orm;

import backend.academy.linktracker.scrapper.checker.LinkCheckerHandler;
import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.parser.LinkParserHandler;
import backend.academy.linktracker.scrapper.repository.jpa.LinkJpaRepository;
import backend.academy.linktracker.scrapper.service.AbstractLinkUpdateService;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public class OrmLinkUpdateService extends AbstractLinkUpdateService {
    private final LinkJpaRepository repository;

    public OrmLinkUpdateService(
            LinkJpaRepository repository, LinkParserHandler parserHandler, LinkCheckerHandler checkerHandler) {
        super(parserHandler, checkerHandler);
        this.repository = repository;
    }

    @Override
    @Transactional
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
