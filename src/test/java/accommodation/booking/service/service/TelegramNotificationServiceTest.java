package accommodation.booking.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import accommodation.booking.service.service.notification.TelegramNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.meta.generics.TelegramBot;

@SpringBootTest
@EnableAsync
@TestPropertySource(properties = {
        "telegram.bot.token=test-token",
        "telegram.chat.id=test-chat-id"
})
public class TelegramNotificationServiceTest {

    @Autowired
    private TelegramNotificationService notificationService;

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private TelegramBotsApi telegramBotsApi;

    @BeforeEach
    void setUp() throws TelegramApiException {
        when(telegramBotsApi.registerBot(any(LongPollingBot.class))).thenReturn(mock(BotSession.class));
    }

    @Test
    @DisplayName("Send notification successfully sends Telegram message")
    void sendNotification_ValidMessage_SendsMessage() throws TelegramApiException {
        // Given: A valid message
        String message = "Test notification";

        // When: Send notification
        notificationService.sendNotification(message);

        // Then: Verify the message was sent
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Send notification with Telegram API error throws RuntimeException")
    void sendNotification_TelegramApiError_ThrowsRuntimeException() throws TelegramApiException {
        // Given: Telegram API throws an exception
        String message = "Test notification";
        TelegramApiException exception = new TelegramApiException("API error");
        when(telegramBot.execute(any(SendMessage.class))).thenThrow(exception);

        // When/Then: Verify that a RuntimeException is thrown
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> notificationService.sendNotification(message));

        // Then: Verify the exception message
        assertThat(thrown.getMessage()).contains("Failed to send Telegram notification");
        assertThat(thrown.getCause()).isEqualTo(exception);
        verify(telegramBot).execute(any(SendMessage.class));
    }
}
