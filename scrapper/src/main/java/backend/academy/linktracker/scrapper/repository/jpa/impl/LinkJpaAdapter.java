package backend.academy.linktracker.scrapper.repository.jpa.impl;

import backend.academy.linktracker.scrapper.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinkJpaAdapter implements LinkRepository {}
