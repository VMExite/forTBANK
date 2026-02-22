package backend.academy.linktracker.bot.command;

import lombok.Getter;

@Getter
public enum CommandName {
    START("/start", "bot.start.description"),
    HELP("/help", "bot.help.description");
    private final String name;
    private final String descriptionKey;

    CommandName(String name, String descriptionKey) {
        this.name = name;
        this.descriptionKey = descriptionKey;
    }
}
