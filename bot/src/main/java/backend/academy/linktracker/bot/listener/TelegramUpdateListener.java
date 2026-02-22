package backend.academy.linktracker.bot.listener;

import backend.academy.linktracker.bot.command.CommandHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramUpdateListener {
    private final TelegramBot bot;
    private final CommandHandler handler;

    @PostConstruct
    public void init() {

        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                handler.handle(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
