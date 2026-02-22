package backend.academy.linktracker.bot.command;

public enum CommandName {
    START("/start", "bot.start.description"),
    HELP("/help", "bot.help.description");
    private final String name;
    private final String descriptionKey;

    private CommandName(String name, String descriptionKey) {
        this.name = name;
        this.descriptionKey = descriptionKey;
    }

    public String getName() {
        return name;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }
}
