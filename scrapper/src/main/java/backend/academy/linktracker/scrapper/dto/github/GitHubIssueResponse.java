package backend.academy.linktracker.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GitHubIssueResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("body") String body,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("user") User user,
        @JsonProperty("pull_request") PullRequest pullRequest) {
    public record User(@JsonProperty("login") String login) {}

    public record PullRequest(@JsonProperty("url") String url) {}
}
