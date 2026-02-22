package backend.academy.linktracker.bot.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalisationService {
    private final MessageSource messageSource;
    // Этого не было в требованиях, но я захотел реализовать локализацию в боте
    public String getMessage(String key, String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);
        return messageSource.getMessage(key, null, locale);
    }

    public String getMessage(String key) {
        return messageSource.getMessage(key, null, Locale.ENGLISH);
    }
}
