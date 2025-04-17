package accommodationbookingservice.service.booking;

import accommodationbookingservice.model.Accommodation;
import accommodationbookingservice.model.Booking;
import accommodationbookingservice.repository.BookingRepository;
import accommodationbookingservice.service.notification.NotificationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingExpirationService {
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * *") // Midnight daily
    public void checkExpiredBookings() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<Booking> expiredBookings =
                bookingRepository.findAllByCheckOutDateLessThanEqualAndStatusNot(
                tomorrow, Booking.BookingStatus.CANCELED
        );

        if (expiredBookings.isEmpty()) {
            notificationService.sendNotification("No expired bookings today!");
            return;
        }

        for (Booking booking : expiredBookings) {
            booking.setStatus(Booking.BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            Accommodation accommodation = booking.getAccommodation();
            notificationService.sendNotification(
                    String.format("Booking expired: ID=%d, Accommodation=%s, Dates=%s to %s",
                            booking.getId(),
                            accommodation.getId(),
                            booking.getCheckInDate(),
                            booking.getCheckOutDate())
            );
            notificationService.sendNotification(
                    String.format("Accommodation released: ID=%d, Type=%s, Location=%s",
                            accommodation.getId(),
                            accommodation.getType(),
                            accommodation.getLocation())
            );
        }
    }
}
