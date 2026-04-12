package backend.academy.linktracker.scrapper.webclient.stackoverflow;

import backend.academy.linktracker.scrapper.dto.stackoverflow.StackExchangeResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowQuestionResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface StackOverflowClient {

    @GetExchange("/questions/{id}")
    StackExchangeResponse<StackOverflowQuestionResponse> getQuestion(@PathVariable Long id);

    @GetExchange("/questions/{id}/answers")
    StackExchangeResponse<StackOverflowAnswerResponse> getAnswers(@PathVariable Long id);

    @GetExchange("/posts/{ids}/comments")
    StackExchangeResponse<StackOverflowCommentResponse> getComments(@PathVariable String ids);
}
