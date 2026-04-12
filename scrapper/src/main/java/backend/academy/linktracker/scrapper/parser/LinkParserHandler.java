package backend.academy.linktracker.scrapper.parser;

import backend.academy.linktracker.scrapper.dto.ParsedLink;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkParserHandler {
    private final List<LinkParser> parsers;

    /**
     * try all parsers and execute first capable of parsing
     * @return interface ParsedLink if LinkParser not supports returns null
     * @see LinkParser
     * @see ParsedLink**/
    public ParsedLink parse(String url) {
        URI uri = URI.create(url);

        for (LinkParser parser : parsers) {
            if (parser.canParse(uri)) {
                return parser.parse(uri);
            }
        }
        return null;
    }
}
