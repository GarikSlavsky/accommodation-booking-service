package accommodationbookingservice.service.notification;

import accommodationbookingservice.config.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class TelegramNotificationService implements NotificationService {
    private final String chatId;
    private final TelegramBot bot;

    public TelegramNotificationService(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.chat.id}") String chatId
    ) throws TelegramApiException {

        if (botToken == null || chatId == null) {
            throw new IllegalStateException(
                    "TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID not found in .env");
        }
        this.chatId = chatId;
        this.bot = new TelegramBot(botToken);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this.bot);
    }

    @Override
    public void sendNotification(String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(
                    "Failed to send Telegram notification: " + e.getMessage(), e);
        }
    }
}
