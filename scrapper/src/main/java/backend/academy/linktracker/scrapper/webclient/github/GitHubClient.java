package backend.academy.linktracker.scrapper.webclient.github;

import backend.academy.linktracker.scrapper.dto.GitHubRepositoryResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface GitHubClient {
    @GetExchange("/repos/{owner}/{repo}")
    GitHubRepositoryResponse getRepository(@PathVariable String owner, @PathVariable String repo);
}
