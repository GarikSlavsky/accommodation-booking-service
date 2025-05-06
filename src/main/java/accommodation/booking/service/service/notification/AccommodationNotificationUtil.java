package accommodation.booking.service.service.notification;

import accommodation.booking.service.model.Accommodation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccommodationNotificationUtil {
    private static final Logger logger =
            LoggerFactory.getLogger(AccommodationNotificationUtil.class);
    private final NotificationService notificationService;

    public void notifyAccommodationReleased(Accommodation accommodation) {
        try {
            notificationService.sendNotification(
                    String.format("Accommodation released: ID=%d, Type=%s, Location=%s",
                            accommodation.getId(),
                            accommodation.getType(),
                            accommodation.getLocation())
            );
        } catch (Exception e) {
            logger.error(
                    "Error sending accommodation release notification for Accommodation ID: {}",
                    accommodation.getId(), e);
        }
    }

    public void notifyAccommodationCreated(Accommodation accommodation) {
        try {
            notificationService.sendNotification(
                    String.format("New accommodation created: ID=%d, Type=%s, Location=%s",
                            accommodation.getId(),
                            accommodation.getType(),
                            accommodation.getLocation())
            );
        } catch (Exception e) {
            logger.error(
                    "Error sending accommodation create notification for Accommodation ID: {}",
                    accommodation.getId(), e);
        }
    }
}
