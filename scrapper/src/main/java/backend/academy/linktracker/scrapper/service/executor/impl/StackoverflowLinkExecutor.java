package backend.academy.linktracker.scrapper.service.executor.impl;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.dto.ParsedLink;
import backend.academy.linktracker.scrapper.dto.StackOverflowLink;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowQuestionResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.webclient.stackoverflow.StackOverflowClient;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class StackoverflowLinkExecutor extends AbstractLinkExecutor {
    private final StackOverflowClient client;

    @Override
    public List<LinkUpdateMessage> fetchEvents(ParsedLink parsedLink, Link link) {
        StackOverflowLink stackOverflowLink = (StackOverflowLink) parsedLink;

        try {
            StackOverflowQuestionResponse question = client.getQuestion(stackOverflowLink.questionId()).items().stream()
                    .findFirst()
                    .orElseThrow(
                            () -> new IllegalStateException("Question not found: " + stackOverflowLink.questionId()));

            List<StackOverflowAnswerResponse> answers =
                    client.getAnswers(stackOverflowLink.questionId()).items();

            List<LinkUpdateMessage> result = new ArrayList<>();

            for (StackOverflowAnswerResponse answer : answers) {
                result.add(new LinkUpdateMessage(
                        null,
                        link.getLinkId().value(),
                        question.title(),
                        answer.owner().displayName(),
                        preview(answer.body()),
                        link.getUrl(),
                        toOffsetDateTime(answer.creationDate())));
            }

            if (!answers.isEmpty()) {
                String answerIds =
                        answers.stream().map(a -> a.answerId().toString()).collect(Collectors.joining(";"));

                List<StackOverflowCommentResponse> answerComments =
                        client.getComments(answerIds).items();

                for (StackOverflowCommentResponse comment : answerComments) {
                    result.add(new LinkUpdateMessage(
                            null,
                            link.getLinkId().value(),
                            question.title(),
                            comment.owner().displayName(),
                            preview(comment.body()),
                            link.getUrl(),
                            toOffsetDateTime(comment.creationDate())));
                }
            }

            List<StackOverflowCommentResponse> questionComments = client.getComments(
                            stackOverflowLink.questionId().toString())
                    .items();

            for (StackOverflowCommentResponse comment : questionComments) {
                result.add(new LinkUpdateMessage(
                        null,
                        link.getLinkId().value(),
                        question.title(),
                        comment.owner().displayName(),
                        preview(comment.body()),
                        comment.link(),
                        toOffsetDateTime(comment.creationDate())));
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch StackOverflow events for questionId={}", stackOverflowLink.questionId(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean supports(ParsedLink link) {
        return link instanceof StackOverflowLink;
    }

    private OffsetDateTime toOffsetDateTime(long epochSeconds) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }
}
