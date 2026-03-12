package backend.academy.linktracker.bot.service;

import java.net.URI;
import java.net.URISyntaxException;

public final class LinkValidator {
    private LinkValidator() {}

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(value);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return false;
            }
            return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}
