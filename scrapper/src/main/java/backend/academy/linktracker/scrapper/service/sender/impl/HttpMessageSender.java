package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import backend.academy.linktracker.scrapper.webclient.bot.BotClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HttpMessageSender implements MessageSender {
    private final BotClient client;

    @Override
    public void sendMessage(LinkUpdateMessage request) {
        client.sendUpdate(request);
    }
}
