package backend.academy.linktracker.scrapper.service.crud;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.model.Link;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkUpdateService {
    List<Link> getBatch(int size, OffsetDateTime before);
    void saveLastUpdates(Link link,List<LinkUpdateMessage> messages);
}
