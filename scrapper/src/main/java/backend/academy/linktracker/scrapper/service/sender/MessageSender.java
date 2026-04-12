package backend.academy.linktracker.scrapper.service.sender;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;

// strategy
public interface MessageSender {
    void sendMessage(LinkUpdateMessage message);
}
