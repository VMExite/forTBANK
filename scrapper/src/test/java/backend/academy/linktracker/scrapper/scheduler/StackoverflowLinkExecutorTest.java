package backend.academy.linktracker.scrapper.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.dto.StackOverflowLink;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackExchangeResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowQuestionResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.value.LinkId;
import backend.academy.linktracker.scrapper.service.executor.impl.StackoverflowLinkExecutor;
import backend.academy.linktracker.scrapper.webclient.stackoverflow.StackOverflowClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StackoverflowLinkExecutorTest {
    @Mock
    private StackOverflowClient client;

    @InjectMocks
    private StackoverflowLinkExecutor executor;

    @Test
    void testGreenAnswerAndComments() {
        Link link = Link.builder()
                .linkId(new LinkId(1L))
                .url("https://stackoverflow.com/questions/123")
                .build();

        StackOverflowLink parsed = new StackOverflowLink(123L);

        when(client.getQuestion(123L))
                .thenReturn(new StackExchangeResponse<>(
                        List.of(new StackOverflowQuestionResponse(
                                123L, "How to test executor?", "https://stackoverflow.com/questions/123")),
                        false,
                        100));
        when(client.getAnswers(123L))
                .thenReturn(new StackExchangeResponse<>(
                        List.of(new StackOverflowAnswerResponse(
                                10L,
                                123L,
                                "How to test executor?",
                                "This is answer body",
                                "https://stackoverflow.com/a/10",
                                1700000000L,
                                new StackOverflowAnswerResponse.Owner("answer_user"))),
                        false,
                        100));
        when(client.getComments("10"))
                .thenReturn(new StackExchangeResponse<>(
                        List.of(new StackOverflowCommentResponse(
                                1L,
                                10L,
                                "answer",
                                "Answer comment",
                                "https://stackoverflow.com/a/10#comment",
                                1700000001L,
                                new StackOverflowCommentResponse.Owner("comment_user"))),
                        false,
                        100));
        when(client.getComments("123"))
                .thenReturn(new StackExchangeResponse<>(
                        List.of(new StackOverflowCommentResponse(
                                2L,
                                123L,
                                "question",
                                "Question comment",
                                "https://stackoverflow.com/questions/123#comment",
                                1700000002L,
                                new StackOverflowCommentResponse.Owner("question_user"))),
                        false,
                        100));
        List<LinkUpdateMessage> result = executor.fetchEvents(parsed, link);

        assertEquals(3, result.size());
        LinkUpdateMessage answer = result.getFirst();
        assertEquals("How to test executor?", answer.title());
        assertEquals("answer_user", answer.username());
        assertEquals("This is answer body", answer.preview());
        assertEquals("https://stackoverflow.com/questions/123", answer.url());

        LinkUpdateMessage answerComment = result.get(1);
        assertEquals("comment_user", answerComment.username());
        assertEquals("Answer comment", answerComment.preview());
        assertEquals("https://stackoverflow.com/questions/123", answerComment.url());

        LinkUpdateMessage questionComment = result.get(2);
        assertEquals("question_user", questionComment.username());
        assertEquals("Question comment", questionComment.preview());
        assertEquals("https://stackoverflow.com/questions/123#comment", questionComment.url());
    }
}
