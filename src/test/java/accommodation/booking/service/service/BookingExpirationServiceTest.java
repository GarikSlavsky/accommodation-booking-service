package accommodation.booking.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.repository.BookingRepository;
import accommodation.booking.service.service.booking.BookingExpirationService;
import accommodation.booking.service.service.notification.AccommodationNotificationUtil;
import accommodation.booking.service.service.notification.BookingNotificationUtil;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BookingExpirationServiceTest {
    private static final Long BOOKING_ID = 1L;
    private static final Long ACCOMMODATION_ID = 1L;
    private Booking booking;
    private Accommodation accommodation;
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingNotificationUtil bookingNotificationUtil;

    @Mock
    private AccommodationNotificationUtil accommodationNotificationUtil;

    @InjectMocks
    private BookingExpirationService bookingExpirationService;

    @BeforeEach
    void setUp() {
        accommodation = initializeAccommodation();
        booking = initializeBooking();
    }

    @Test
    @DisplayName("Check expired bookings marks bookings as EXPIRED and sends notifications")
    void checkExpiredBookings_ExpiredBookingsFound_MarksExpiredAndNotifies() {
        // Given: One expired booking is found
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        when(bookingRepository.findAllByCheckOutDateLessThanEqualAndStatusNot(
                tomorrow, Booking.BookingStatus.CANCELED
        )).thenReturn(List.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        // When: Call the service method
        bookingExpirationService.checkExpiredBookings();

        // Then: Verify the booking is marked EXPIRED and notifications are sent
        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.EXPIRED);
        verify(bookingRepository).findAllByCheckOutDateLessThanEqualAndStatusNot(
                tomorrow, Booking.BookingStatus.CANCELED
        );
        verify(bookingRepository).save(booking);
        verify(bookingNotificationUtil).notifyBookingExpired(booking, accommodation);
        verify(accommodationNotificationUtil).notifyAccommodationReleased(accommodation);
        verify(bookingNotificationUtil, never()).notifyNoExpiredBooking();
    }

    private Accommodation initializeAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(ACCOMMODATION_ID);
        return accommodation;
    }

    private Booking initializeBooking() {
        Booking booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setAccommodation(accommodation);
        booking.setCheckInDate(LocalDate.now().minusDays(5));
        booking.setCheckOutDate(LocalDate.now());
        booking.setStatus(Booking.BookingStatus.PENDING);
        return booking;
    }
}
