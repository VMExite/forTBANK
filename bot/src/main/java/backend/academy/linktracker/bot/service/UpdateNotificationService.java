package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateNotificationService {
    private final TelegramBot bot;
    private final LocalisationService localisationService;

    public void notifyUsers(LinkUpdate update) {
        if (update == null || update.tgChatIds() == null) {
            return;
        }
        String text = buildMessage(update);
        List<Long> chatIds = update.tgChatIds();
        for (Long chatId : chatIds) {
            if (chatId == null) {
                continue;
            }
            bot.execute(new SendMessage(chatId, text));
        }
        log.info("update_sent url={} chats={}", update.url(), chatIds.size());
    }

    private String buildMessage(LinkUpdate update) {
        String base = localisationService.getMessage("bot.update.notification");
        if (update.description() == null || update.description().isBlank()) {
            return base + " " + update.url();
        }
        return base + " " + update.url() + "\n" + update.description();
    }
}
