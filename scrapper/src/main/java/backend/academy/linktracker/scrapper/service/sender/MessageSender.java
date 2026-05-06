package backend.academy.linktracker.scrapper.service.sender;

import backend.academy.linktracker.scrapper.dto.LinkUpdateMessage;
import java.util.concurrent.CompletableFuture;

// strategy
public interface MessageSender {
    CompletableFuture sendMessage(LinkUpdateMessage message);
}
