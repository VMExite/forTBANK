package backend.academy.linktracker.scrapper.checker.impl;

import backend.academy.linktracker.scrapper.checker.LinkChecker;
import backend.academy.linktracker.scrapper.dto.GitHubLink;
import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.webclient.github.GitHubClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class GitHubLinkChecker implements LinkChecker {

    private final GitHubClient client;

    @Override
    public boolean canCheck(ParsedLink link) {
        return link instanceof GitHubLink;
    }

    @Override
    public OffsetDateTime getLastUpdate(ParsedLink link) {
        GitHubLink gitHubLink = (GitHubLink) link;
        return client
            .getRepository(gitHubLink.owner(), gitHubLink.repo())
            .updated_at();
    }
}
