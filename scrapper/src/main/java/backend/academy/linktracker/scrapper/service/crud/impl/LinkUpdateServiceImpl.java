package backend.academy.linktracker.scrapper.service.crud.impl;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.crud.LinkUpdateService;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LinkUpdateServiceImpl implements LinkUpdateService {
    private final LinkRepository linkRepository;

    @Override
    public List<Link> getBatch(int size, OffsetDateTime before) {
        log.info("batch requested: size={}", size);
        return linkRepository.findBatch(size, before);
    }

    @Override
    @Transactional
    public void saveLastUpdates(Link link, List<LinkUpdateMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        OffsetDateTime lastUpdate = messages.stream()
                .map(LinkUpdateMessage::createdAt)
                .max(Comparator.naturalOrder())
                .orElse(link.getLastUpdate());

        link.setLastUpdate(lastUpdate);
        linkRepository.updateLastUpdate(link);
        log.info("lastUpdate updated: linkId={}, lastUpdate={}", link.getLinkId(), lastUpdate);
    }
}
