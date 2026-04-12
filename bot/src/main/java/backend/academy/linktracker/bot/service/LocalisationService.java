package backend.academy.linktracker.bot.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalisationService {
    private final MessageSource messageSource;

    public String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key,args, Locale.ENGLISH);
    }
}
