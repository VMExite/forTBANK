package backend.academy.linktracker.bot.command;

import lombok.Getter;

@Getter
public enum CommandName {
    START("/start", "bot.start.description"),
    HELP("/help", "bot.help.description"),
    TRACK("/track", "bot.track.description"),
    UNTRACK("/untrack", "bot.untrack.description"),
    LIST("/list", "bot.list.description"),
    CANCEL("/cancel", "bot.cancel.description");
    private final String name;
    private final String descriptionKey;

    CommandName(String name, String descriptionKey) {
        this.name = name;
        this.descriptionKey = descriptionKey;
    }
}
