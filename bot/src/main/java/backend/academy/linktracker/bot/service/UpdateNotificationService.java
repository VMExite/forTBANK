package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Strings;
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
        StringBuilder builder = new StringBuilder(localisationService.getMessage("bot.update.notification"));
        builder.append(update.url());

        if (update.description() != null && !update.description().isEmpty()) {
            builder.append(Strings.lineSeparator());
            builder.append(update.description());
        }
        return builder.toString();
    }
}
