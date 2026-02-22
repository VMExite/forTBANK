package backend.academy.linktracker.bot.command.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.command.CommandName;
import backend.academy.linktracker.bot.service.LocalisationService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelpCommandTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private LocalisationService localisationService;

    @InjectMocks
    private HelpCommand helpCommand;

    @Test
    void shouldReturnTrueCanHandleMethod() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(CommandName.HELP.getName());

        boolean result = helpCommand.canHandle(update);

        assertTrue(result);
    }

    @Test
    void shouldSendHelpMessage() {
        Long chatId = 1L;
        String lang = Locale.ENGLISH.getLanguage();
        String expectedMessage = "Help message";

        Update updateMock = mock(Update.class);
        Message messageMock = mock(Message.class);
        Chat chatMock = mock(Chat.class);
        User userMock = mock(User.class);

        when(updateMock.message()).thenReturn(messageMock);
        when(messageMock.chat()).thenReturn(chatMock);
        when(chatMock.id()).thenReturn(chatId);
        when(messageMock.from()).thenReturn(userMock);
        when(userMock.languageCode()).thenReturn(lang);

        when(localisationService.getMessage("bot.help", lang)).thenReturn(expectedMessage);
        helpCommand.handle(updateMock);

        verify(bot)
                .execute(argThat(msg -> msg.getParameters().get("chat_id").equals(chatId)
                        && msg.getParameters().get("text").equals(expectedMessage)));

        verify(localisationService).getMessage("bot.help", lang);
    }
}
