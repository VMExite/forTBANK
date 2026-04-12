package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.dto.GitHubLink;
import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.dto.github.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.LinkId;
import backend.academy.linktracker.scrapper.service.executor.impl.GitHubLinkExecutor;
import backend.academy.linktracker.scrapper.webclient.github.GitHubClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubExecutorTest {
    @Mock
    private GitHubClient gitHubClient;
    @InjectMocks
    private GitHubLinkExecutor executor;

    @Test
    void testGreenNewIssue() throws Exception {
        Link link = Link.builder()
            .linkId(new LinkId(1L))
            .url("https://github.com/test/repo/issues/1")
            .build();
        GitHubLink parsed = new GitHubLink("test","repo");

        when(gitHubClient.getIssues(parsed.owner(),parsed.repo()))
            .thenReturn(List.of(
                new GitHubIssueResponse(
                    1L,
                    "New feature",
                    "This is a new issue description",
                    OffsetDateTime.now(),
                    new GitHubIssueResponse.User("test_user"),
                    new GitHubIssueResponse.PullRequest(null)
                )));

        List<LinkUpdateMessage> result = executor.execute(link, parsed);


        assertFalse(result.isEmpty());
        LinkUpdateMessage message = result.getFirst();

        assertEquals("New feature", message.title());
        assertEquals("test_user", message.username());
        assertEquals("This is a new issue description", message.preview());
    }
}
