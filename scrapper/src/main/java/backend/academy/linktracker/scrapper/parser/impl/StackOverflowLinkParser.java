package backend.academy.linktracker.scrapper.parser.impl;

import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.dto.StackOverflowLink;
import backend.academy.linktracker.scrapper.parser.LinkParser;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
public class StackOverflowLinkParser implements LinkParser {
    @Override
    public boolean canParse(URI uri) {
        return "stackoverflow.com".equals(uri.getHost());
    }

    @Override
    public ParsedLink parse(URI uri) {
        String[] segments = uri.getPath().split("/");

        if (segments.length < 3 || !"questions".equals(segments[1])) {
            throw new IllegalArgumentException("Невалидная StackOverflow URL");
        }

        long id = Long.parseLong(segments[2]);
        return new StackOverflowLink(id);
    }
}
