package backend.academy.linktracker.scrapper.checker;

import backend.academy.linktracker.scrapper.dto.ParsedLink;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkCheckerHandler {
    private final List<LinkChecker> checkers;

    public OffsetDateTime check(ParsedLink link) {
        for (LinkChecker checker : checkers) {
            if (checker.canCheck(link)) {
                return checker.getLastUpdate(link);
            }
        }
        throw new UnsupportedOperationException("Чекер не найден");
    }
}
