package backend.academy.linktracker.scrapper.repository.jpa.impl;

import backend.academy.linktracker.scrapper.mapper.LinkMapper;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.LinkJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinkJpaAdapter implements LinkRepository {
    private final LinkJpaRepository linkJpaRepository;
    private final LinkMapper linkMapper;

    @Override
    public List<Link> findBatch(int size, OffsetDateTime before) {
        Pageable pageable = PageRequest.of(0, size);
        return linkJpaRepository.findBatch(before, pageable).stream()
                .map(linkMapper::fromEntity)
                .toList();
    }

    @Override
    public void updateAll(List<Link> links) {

    }
}
