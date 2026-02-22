package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;

public interface Command {
    default boolean canHandle(Update update) {
        return update.message() != null && !update.message().text().isEmpty();
    }

    void handle(Update update);

    String getCommandName();

    String getDescriptionKey();
}
