package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.CommandName;
import backend.academy.linktracker.bot.service.LocalisationService;
import backend.academy.linktracker.bot.service.TrackConversationService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackCommand implements Command {
    private final TelegramBot bot;
    private final LocalisationService localisationService;
    private final TrackConversationService trackConversationService;

    @Override
    public boolean canHandle(Update update) {
        return Command.super.canHandle(update) && update.message().text().startsWith(getCommandName());
    }

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();
        String lang = update.message().from().languageCode();

        trackConversationService.start(chatId);
        String text = localisationService.getMessage("bot.track.ask-link", lang);
        bot.execute(new SendMessage(chatId, text));
    }

    @Override
    public String getCommandName() {
        return CommandName.TRACK.getName();
    }

    @Override
    public String getDescriptionKey() {
        return CommandName.TRACK.getDescriptionKey();
    }
}
