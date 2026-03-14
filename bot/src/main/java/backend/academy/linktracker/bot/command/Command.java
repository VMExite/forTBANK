package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;

public interface Command {
    default boolean canHandle(Update update) {
        return update.message() != null && !update.message().text().isEmpty();
    }

    default String extractArgument(String text, int partsCount, int targetPartNumber) {
        String[] parts = text.split("\\s+", partsCount);
        if (parts.length < partsCount) {
            return "";
        }
        return parts[targetPartNumber].trim();
    }

    void handle(Update update);

    String getCommandName();

    String getDescriptionKey();
}
