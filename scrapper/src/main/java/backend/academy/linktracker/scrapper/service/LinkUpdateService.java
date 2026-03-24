package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import java.util.List;

public interface LinkUpdateService {
    List<LinkUpdateRequest> updateLinks();
}
