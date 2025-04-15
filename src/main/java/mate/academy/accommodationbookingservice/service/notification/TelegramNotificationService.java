package mate.academy.accommodationbookingservice.service.notification;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class TelegramNotificationService implements NotificationService {
    private final String botToken;
    private final String chatId;
    private final TelegramBot bot;

    public TelegramNotificationService() throws TelegramApiException {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // Don’t fail if .env is missing (optional)
                .load();
        this.botToken = dotenv.get("TELEGRAM_BOT_TOKEN");
        this.chatId = dotenv.get("TELEGRAM_CHAT_ID");

        if (botToken == null || chatId == null) {
            throw new IllegalStateException(
                    "TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID not found in .env");
        }

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

    private static class TelegramBot extends org.telegram.telegrambots.bots.TelegramLongPollingBot {
        public TelegramBot(String botToken) {
            super(botToken);
        }

        @Override
        public String getBotUsername() {
            return "BookingBot";
        }

        @Override
        public void onUpdateReceived(Update update) {
            // No action needed for notifications, but required to implement
        }
    }
}
