package backend.academy.linktracker.scrapper.webclient.stackoverflow;

import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface StackOverflowClient {
    @GetExchange("/questions/{id}?site=stackoverflow")
    StackOverflowResponse getQuestion(@PathVariable Long id);
}
