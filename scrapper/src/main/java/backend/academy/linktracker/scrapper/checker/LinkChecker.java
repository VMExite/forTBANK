package backend.academy.linktracker.scrapper.checker;

import backend.academy.linktracker.scrapper.dto.ParsedLink;
import java.time.OffsetDateTime;

public interface LinkChecker {
    boolean canCheck(ParsedLink link);

    OffsetDateTime getLastUpdate(ParsedLink link);
}
