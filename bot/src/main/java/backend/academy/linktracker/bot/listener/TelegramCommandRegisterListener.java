package backend.academy.linktracker.bot.listener;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.service.LocalisationService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramCommandRegisterListener {
    private final TelegramBot bot;
    private final List<Command> commands;
    private final LocalisationService localisationService;

    private final static List<String> SUPPORTED_LANDS = List.of("en", "ru");
    // Тут я хотел зарегистрировать команды на всех языках, так не работает((
    // Todo: переделать метод, оставить только английский

    @EventListener(ApplicationReadyEvent.class)
    public void setMyCommands() {
        for (String lang : SUPPORTED_LANDS) {
            BotCommand[] botCommands = commands.stream()
                .map(command ->
                    new BotCommand(
                        command.getCommandName(),
                        localisationService.getMessage(command.getDescriptionKey(), lang)
                    )
                ).toArray(BotCommand[]::new);
            bot.execute(new SetMyCommands(botCommands).languageCode(lang));
        }
    }

}
