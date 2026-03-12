package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.service.LocalisationService;
import backend.academy.linktracker.bot.service.TrackConversationService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {
    private final Collection<Command> commands;
    private final TelegramBot bot;
    private final LocalisationService localisationService;
    private final TrackConversationService trackConversationService;

    public void handle(Update update) {
        if (update == null) {
            return;
        }

        if (update.message() != null && update.message().text() != null) {
            long chatId = update.message().chat().id();
            String text = update.message().text().trim();

            if (trackConversationService.isActive(chatId) && text.startsWith("/")) {
                trackConversationService.cancel(chatId);
            }

            if (trackConversationService.tryHandle(update)) {
                return;
            }
        }

        for (Command command : commands) {
            if (command.canHandle(update)) {
                command.handle(update);
                log.info(
                        "command_received command={} chatId={} locale={}",
                        command.getCommandName(),
                        update.message().chat().id(),
                        update.message().from().languageCode());
                return;
            }
        }
        if (update.message() != null && !update.message().text().isEmpty()) {
            long chatId = update.message().chat().id();
            String lang = update.message().from().languageCode();

            String text = localisationService.getMessage("bot.unknown", lang);
            bot.execute(new SendMessage(chatId, text));
        }
    }
}
