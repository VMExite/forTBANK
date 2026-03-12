package backend.academy.linktracker.scrapper.parser.impl;

import backend.academy.linktracker.scrapper.dto.GitHubLink;
import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.parser.LinkParser;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class GitHubLinkParser implements LinkParser {
    @Override
    public boolean canParse(URI uri) {
        return "github.com".equals(uri.getHost());
    }

    @Override
    public ParsedLink parse(URI uri) {
        String[] segments = uri.getPath().split("/");
        if (segments.length < 3) {
            throw new IllegalArgumentException("Невалидный GitHub URL");
        }

        String owner = segments[1];
        String repo = segments[2];

        return new GitHubLink(owner, repo);
    }
}
