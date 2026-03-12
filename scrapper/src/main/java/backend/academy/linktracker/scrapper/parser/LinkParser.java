package backend.academy.linktracker.scrapper.parser;

import backend.academy.linktracker.scrapper.dto.ParsedLink;
import java.net.URI;

public interface LinkParser {
    boolean canParse(URI uri);
    ParsedLink parse(URI uri);
}
