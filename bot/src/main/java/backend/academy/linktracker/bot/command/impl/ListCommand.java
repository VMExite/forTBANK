package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.CommandName;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinkResponse;
import backend.academy.linktracker.bot.service.LocalisationService;
import backend.academy.linktracker.bot.webclient.ScrapperClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class ListCommand implements Command {
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
        String tag = extractArgument(update.message().text().trim(), 2, 1);

        try {
            ListLinkResponse response = scrapperClient.getLinks(chatId);
            List<LinkResponse> links = response.links();
            if (!tag.isEmpty()) {
                links = filterByTag(links, tag);
            }

            if (links == null || links.isEmpty()) {
                String message = localisationService.getMessage("bot.list.empty", lang);
                bot.execute(new SendMessage(chatId, message));
                return;
            }

            String message = formatList(links, lang);
            bot.execute(new SendMessage(chatId, message));
        } catch (RestClientResponseException ex) {
            String key =
                    switch (ex.getStatusCode().value()) {
                        case 404 -> "bot.list.chat-not-registered";
                        default -> "bot.list.error";
                    };
            String message = localisationService.getMessage(key, lang);
            bot.execute(new SendMessage(chatId, message));
        }
    }

    @Override
    public String getCommandName() {
        return CommandName.LIST.getName();
    }

    @Override
    public String getDescriptionKey() {
        return CommandName.LIST.getDescriptionKey();
    }

    private List<LinkResponse> filterByTag(List<LinkResponse> links, String tag) {
        if (links == null || tag.isBlank()) {
            return links;
        }
        String expected = tag.toLowerCase();
        return links.stream()
                .filter(link -> link.tags() != null
                        && link.tags().stream()
                                .anyMatch(t -> t != null && t.toLowerCase().equals(expected)))
                .toList();
    }

    private String formatList(List<LinkResponse> links, String lang) {
        StringBuilder builder = new StringBuilder(localisationService.getMessage("bot.list.header", lang));
        builder.append("\n");

        for (LinkResponse link : links) {
            builder.append("-");
            builder.append(link.url());
            builder.append("\n");
        }

        return builder.toString();
    }
}
