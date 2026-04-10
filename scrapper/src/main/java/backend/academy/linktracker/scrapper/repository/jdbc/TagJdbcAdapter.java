package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagJdbcAdapter implements TagRepository {}
