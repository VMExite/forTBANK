package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.webclient.bot.BotClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkUpdateRestClientService {
    private final BotClient client;

    public void updateLinks(List<LinkUpdateRequest> request) {
        for (LinkUpdateRequest linkUpdate: request) {
            client.sendUpdate(linkUpdate);
        }
    }
}
