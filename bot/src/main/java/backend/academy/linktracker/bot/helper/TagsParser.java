package backend.academy.linktracker.bot.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TagsParser {
    private TagsParser() {}

    public static List<String> parse(String tagsLine) {
        if (tagsLine == null || tagsLine.isEmpty()) {
            return new ArrayList<>();
        }

        tagsLine = tagsLine.toLowerCase(Locale.ROOT);
        String[] tags = tagsLine.split(",");
        return Arrays.stream(tags).toList();
    }
}
