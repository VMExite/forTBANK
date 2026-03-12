package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.CommandName;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.service.LinkValidator;
import backend.academy.linktracker.bot.service.LocalisationService;
import backend.academy.linktracker.bot.webclient.ScrapperClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class UntrackCommand implements Command {
    private final TelegramBot bot;
    private final LocalisationService localisationService;
    private final ScrapperClient scrapperClient;

    @Override
    public boolean canHandle(Update update) {
        return Command.super.canHandle(update) && update.message().text().startsWith(getCommandName());
    }

    @Override
    public void handle(Update update) {
        long chatId = update.message().chat().id();
        String lang = update.message().from().languageCode();
        String text = update.message().text().trim();
        String argument = extractArgument(text);

        if (!LinkValidator.isValid(argument)) {
            String message = localisationService.getMessage("bot.untrack.invalid-link", lang);
            bot.execute(new SendMessage(chatId, message));
            return;
        }

        try {
            scrapperClient.removeLink(chatId, new RemoveLinkRequest(argument));
            String message = localisationService.getMessage("bot.untrack.removed", lang);
            bot.execute(new SendMessage(chatId, message));
        } catch (RestClientResponseException ex) {
            String key =
                    switch (ex.getStatusCode().value()) {
                        case 400 -> "bot.untrack.invalid-link";
                        case 404 -> "bot.untrack.not-found";
                        default -> "bot.untrack.error";
                    };
            String message = localisationService.getMessage(key, lang);
            bot.execute(new SendMessage(chatId, message));
        }
    }

    @Override
    public String getCommandName() {
        return CommandName.UNTRACK.getName();
    }

    @Override
    public String getDescriptionKey() {
        return CommandName.UNTRACK.getDescriptionKey();
    }

    private String extractArgument(String text) {
        String[] parts = text.split("\\s+", 2);
        if (parts.length < 2) {
            return "";
        }
        return parts[1].trim();
    }
}
