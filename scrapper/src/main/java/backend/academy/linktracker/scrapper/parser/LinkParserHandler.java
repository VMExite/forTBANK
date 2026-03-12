package backend.academy.linktracker.scrapper.parser;

import backend.academy.linktracker.scrapper.dto.ParsedLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LinkParserHandler {
    private final List<LinkParser> parsers;

    public ParsedLink parse(String url) throws UnsupportedOperationException {
        URI uri = URI.create(url);

        for (LinkParser parser: parsers) {
            if (parser.canParse(uri)) {
                return parser.parse(uri);
            }
        }
        throw new UnsupportedOperationException("Ссылка не поддерживается");
    }
}
