package backend.academy.linktracker.scrapper.checker.impl;

import backend.academy.linktracker.scrapper.checker.LinkChecker;
import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.dto.StackOverflowLink;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.webclient.stackoverflow.StackOverflowClient;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackoverflowLinkChecker implements LinkChecker {

    private StackOverflowClient client;

    @Override
    public boolean canCheck(ParsedLink link) {
        return link instanceof StackOverflowLink;
    }

    @Override
    public OffsetDateTime getLastUpdate(ParsedLink link) {
        StackOverflowLink stackOverflowLink = (StackOverflowLink) link;
        StackOverflowResponse response = client.getQuestion(stackOverflowLink.questionId());

        if (response.items().isEmpty()) {
            throw new IllegalStateException("Вопрос не найден");
        }

        long lastActivityDate = response.items().getFirst().lastActivityDate();

        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(lastActivityDate), ZoneOffset.UTC);
    }
}
