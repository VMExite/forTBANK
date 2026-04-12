package backend.academy.linktracker.scrapper.webclient.github;

import backend.academy.linktracker.scrapper.dto.github.GitHubIssueResponse;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface GitHubClient {
    @GetExchange("/repos/{owner}/{repo}/issues")
    List<GitHubIssueResponse> getIssues(@PathVariable String owner, @PathVariable String repo);
}
