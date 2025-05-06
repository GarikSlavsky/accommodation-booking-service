package accommodation.booking.service.service.notification;

import accommodation.booking.service.model.Payment;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentNotificationUtil {
    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationUtil.class);
    private final NotificationService notificationService;

    public void notifyPaymentCreated(Long bookingId, BigDecimal amountToPay, String url) {
        try {
            notificationService.sendNotification(
                    String.format("Payment initiated for Booking ID=%d: Amount=$%.2f, URL=%s",
                            bookingId, amountToPay, url
            ));
        } catch (Exception e) {
            logger.error("Error sending payment creation notification for Booking ID={}: {}",
                    bookingId, e.getMessage(), e);
        }
    }

    public void notifyOfExpiredSession(Payment payment) {
        try {
            notificationService.sendNotification(
                    String.format("Payment session expired for Booking ID=%d: Amount=$%.2f",
                            payment.getBooking().getId(), payment.getAmountToPay())
            );
        } catch (Exception e) {
            logger.warn("Failed to send notification for payment ID={}: {}",
                    payment.getId(), e.getMessage());
        }
    }

    public void notifyOfSessionRenewed(Long bookingId, BigDecimal amountToPay, String url) {
        try {
            notificationService.sendNotification(
                    String.format("Payment session renewed for Booking ID=%d: Amount=$%.2f, URL=%s",
                            bookingId, amountToPay, url
            ));
        } catch (Exception e) {
            logger.error("Error sending session renewal notification for Booking ID={}: {}",
                    bookingId, e.getMessage(), e);
        }
    }

    public void notifyOfSuccessfulPayment(Payment payment, String sessionId) {
        try {
            notificationService.sendNotification(String.format(
                    "Payment successful for Booking ID=%d: Amount=$%.2f, Session ID=%s",
                    payment.getBooking().getId(), payment.getAmountToPay(), sessionId)
            );
        } catch (Exception e) {
            logger.error("Error sending successful payment notification for Payment ID={}: {}",
                    payment.getId(), e.getMessage(), e);
        }
    }

    public void notifyCancelledPayment(Payment payment) {
        try {
            notificationService.sendNotification(
                    String.format("Payment canceled for Booking ID=%d: Amount=$%.2f",
                            payment.getBooking().getId(), payment.getAmountToPay())
            );
        } catch (Exception e) {
            logger.error("Error sending payment cancellation notification for Payment ID={}: {}",
                    payment.getId(), e.getMessage(), e);
        }
    }
}
