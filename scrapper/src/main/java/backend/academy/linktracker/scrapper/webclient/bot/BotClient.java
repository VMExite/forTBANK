package backend.academy.linktracker.scrapper.webclient.bot;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface BotClient {

    @PostExchange("/updates")
    void sendUpdate(@RequestBody LinkUpdateRequest request);
}
