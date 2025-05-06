package accommodation.booking.service.service.booking;

import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.repository.BookingRepository;
import accommodation.booking.service.service.notification.AccommodationNotificationUtil;
import accommodation.booking.service.service.notification.BookingNotificationUtil;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingExpirationService {
    private static final Logger logger = LoggerFactory.getLogger(BookingExpirationService.class);
    private final BookingRepository bookingRepository;
    private final BookingNotificationUtil bookingNotificationUtil;
    private final AccommodationNotificationUtil accommodationNotificationUtil;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // Midnight daily
    public void checkExpiredBookings() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<Booking> expiredBookings =
                bookingRepository.findAllByCheckOutDateLessThanEqualAndStatusNot(
                        tomorrow, Booking.BookingStatus.CANCELED
                );

        if (expiredBookings.isEmpty()) {
            bookingNotificationUtil.notifyNoExpiredBooking();
            return;
        }
        updateBookingStatus(expiredBookings);
    }

    private void updateBookingStatus(List<Booking> bookingList) {
        for (Booking booking : bookingList) {
            try {
                booking.setStatus(Booking.BookingStatus.EXPIRED);
                bookingRepository.save(booking);
                logger.info("Booking ID={} marked as EXPIRED.", booking.getId());
                Accommodation accommodation = booking.getAccommodation();
                bookingNotificationUtil.notifyBookingExpired(booking, accommodation);
                accommodationNotificationUtil.notifyAccommodationReleased(accommodation);
            } catch (Exception e) {
                logger.error("Failed to process booking ID={} {}", booking.getId(), e.getMessage());
            }
        }
    }
}
