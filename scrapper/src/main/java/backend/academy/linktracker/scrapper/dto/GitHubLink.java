package backend.academy.linktracker.scrapper.dto;

public record GitHubLink(
    String owner,
    String repo
) implements ParsedLink {
}
