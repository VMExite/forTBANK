package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.repository.jdbc.ChatJdbcRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.LinkJdbcRepository;
import backend.academy.linktracker.scrapper.service.sql.SqlLinksService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {
    @Mock
    private LinkJdbcRepository linkRepository;

    @Mock
    private ChatJdbcRepository chatRepository;

    @InjectMocks
    private SqlLinksService linksService;
}
