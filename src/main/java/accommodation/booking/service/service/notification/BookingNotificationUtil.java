package accommodation.booking.service.service.notification;

import accommodation.booking.service.dto.booking.BookingResponseDto;
import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.model.Booking;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingNotificationUtil {
    private static final Logger logger = LoggerFactory.getLogger(BookingNotificationUtil.class);
    private final NotificationService notificationService;

    public void notifyBookingCreated(BookingResponseDto bookingDto) {
        try {
            notificationService.sendNotification(
                    String.format("New booking created: ID=%d, Accommodation=%d, Dates=%s to %s",
                            bookingDto.getId(), bookingDto.getAccommodationId(),
                            bookingDto.getCheckInDate(), bookingDto.getCheckOutDate())
            );
        } catch (Exception e) {
            logger.error("Failed to send notification for booking ID={}", bookingDto.getId(), e);
        }
    }

    public void notifyBookingCancelled(Booking booking, Accommodation accommodation) {
        try {
            notificationService.sendNotification(
                    String.format("Booking canceled: ID=%d, Accommodation=%s, Dates=%s to %s",
                            booking.getId(), accommodation.getId(),
                            booking.getCheckInDate(), booking.getCheckOutDate())
            );
        } catch (Exception e) {
            logger.error("Error sending booking cancellation notification for Booking ID: {}",
                    booking.getId(), e);
        }
    }

    public void notifyBookingExpired(Booking booking, Accommodation accommodation) {
        try {
            notificationService.sendNotification(
                    String.format("Booking expired: ID=%d, Accommodation=%s, Dates=%s to %s",
                            booking.getId(),
                            accommodation.getId(),
                            booking.getCheckInDate(),
                            booking.getCheckOutDate())
            );
        } catch (Exception e) {
            logger.warn("Failed to send notification for booking ID={}: {}",
                    booking.getId(), e.getMessage());
        }
    }

    public void notifyNoExpiredBooking() {
        try {
            notificationService.sendNotification("No expired bookings today!");
        } catch (Exception e) {
            logger.warn("Failed to send notification.");
        }
    }
}
