package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import backend.academy.linktracker.scrapper.webclient.bot.BotClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpMessageSender implements MessageSender {
    private final BotClient client;

    @Override
    public void sendMessage(LinkUpdateMessage request) {
        try {
            client.sendUpdate(request);
        } catch (RestClientException e) {
            log.error("Failed to send update. chatId={}, url={}", request.chatId(), request.url(), e);
            throw e;
        }
    }
}
