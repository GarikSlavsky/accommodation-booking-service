package accommodation.booking.service.config;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBot extends TelegramLongPollingBot {
    public TelegramBot(String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return "BookingBot";
    }

    @Override
    public void onUpdateReceived(Update update) {
    }
}
