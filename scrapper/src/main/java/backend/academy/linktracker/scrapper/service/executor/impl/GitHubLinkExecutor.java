package backend.academy.linktracker.scrapper.service.executor.impl;

import backend.academy.linktracker.scrapper.dto.GitHubLink;
import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.dto.github.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.webclient.github.GitHubClient;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class GitHubLinkExecutor extends AbstractLinkExecutor {
    private final GitHubClient client;

    @Override
    public List<LinkUpdateMessage> fetchEvents(ParsedLink parsedLink, Link link) {
        GitHubLink gitHubLink = (GitHubLink) parsedLink;

        try {
            return client.getIssues(gitHubLink.owner(), gitHubLink.repo()).stream()
                    .sorted(Comparator.comparing(GitHubIssueResponse::createdAt))
                    .map(issue -> new LinkUpdateMessage(
                            null,
                            link.getLinkId().value(),
                            issue.title(),
                            issue.user().login(),
                            preview(issue.body()),
                            link.getUrl(),
                            issue.createdAt()))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch GitHub events for {} / {}", gitHubLink.owner(), gitHubLink.repo(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean supports(ParsedLink link) {
        return link instanceof GitHubLink;
    }
}
