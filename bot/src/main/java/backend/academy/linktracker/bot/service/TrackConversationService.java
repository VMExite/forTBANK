package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.webclient.ScrapperClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
// Todo: переписать с явным разделением ответсвенности и большей рассширяемостью
// (сейчас нельзя нормально использовать grpc в /track из за немаштабируемости компоненты)

@Service
@RequiredArgsConstructor
public class TrackConversationService {
    private final TelegramBot bot;
    private final LocalisationService localisationService;
    private final ScrapperClient scrapperClient;

    private final Map<Long, TrackSession> sessions = new ConcurrentHashMap<>();

    public boolean isActive(long chatId) {
        TrackSession session = sessions.get(chatId);
        return session != null && session.state() != TrackState.NONE;
    }

    public void start(long chatId) {
        sessions.put(chatId, new TrackSession(TrackState.AWAITING_LINK, null));
    }

    public void cancel(long chatId) {
        sessions.remove(chatId);
    }

    public boolean tryHandle(Update update) {
        if (update == null || update.message() == null || update.message().text() == null) {
            return false;
        }
        long chatId = update.message().chat().id();
        TrackSession session = sessions.get(chatId);
        if (session == null || session.state() == TrackState.NONE) {
            return false;
        }

        String text = update.message().text().trim();
        if (text.startsWith("/")) {
            return false;
        }

        String lang = update.message().from().languageCode();

        if (session.state() == TrackState.AWAITING_LINK) {
            if (!LinkValidator.isValid(text)) {
                String message = localisationService.getMessage("bot.track.invalid-link", lang);
                bot.execute(new SendMessage(chatId, message));
                return true;
            }

            sessions.put(chatId, new TrackSession(TrackState.AWAITING_TAGS, text));
            String message = localisationService.getMessage("bot.track.ask-tags", lang);
            bot.execute(new SendMessage(chatId, message));
            return true;
        }

        if (session.state() == TrackState.AWAITING_TAGS) {
            List<String> tags = parseTags(text);
            AddLinkRequest request = new AddLinkRequest(session.link(), tags, List.of());
            try {
                scrapperClient.addLink(chatId, request);
                String message = localisationService.getMessage("bot.track.added", lang);
                bot.execute(new SendMessage(chatId, message));
                sessions.remove(chatId);
            } catch (RestClientResponseException ex) {
                handleTrackError(chatId, lang, ex);
            }
            return true;
        }

        return false;
    }

    private void handleTrackError(long chatId, String lang, RestClientResponseException ex) {
        String key =
                switch (ex.getStatusCode().value()) {
                    case 400 -> "bot.track.invalid-link";
                    case 404 -> "bot.track.chat-not-registered";
                    case 409 -> "bot.track.already-tracked";
                    default -> "bot.track.error";
                };
        String message = localisationService.getMessage(key, lang);
        bot.execute(new SendMessage(chatId, message));
    }

    private List<String> parseTags(String text) {
        if (text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }

    private enum TrackState {
        NONE,
        AWAITING_LINK,
        AWAITING_TAGS
    }

    private record TrackSession(TrackState state, String link) {}
}
