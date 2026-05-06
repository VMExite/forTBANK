package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.dto.LinkUpdateMessage;
import backend.academy.linktracker.bot.exception.NotificationFailedException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateNotificationService {
    private final TelegramBot bot;
    private final LocalisationService localisationService;

    public void notifyUsers(LinkUpdateMessage update) throws NotificationFailedException {
        if (update == null || update.chatId() == null) {
            return;
        }

        String text = buildMessage(update);

        try {
            bot.execute(new SendMessage(update.chatId(), text));
            log.info("update_sent url={} for chat={}", update.url(), update.chatId());
        } catch (Exception e) {
            log.error("update_sent url={} for chat={}", update.url(), update.chatId(), e);
            throw new NotificationFailedException("tg notification failed");
        }
    }

    private String buildMessage(LinkUpdateMessage update) {
        return localisationService.getMessage(
                "bot.update.notification",
                update.title(),
                update.username(),
                update.preview(),
                update.url(),
                update.createdAt());
    }
}
