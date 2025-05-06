package accommodation.booking.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.service.notification.AccommodationNotificationUtil;
import accommodation.booking.service.service.notification.BookingNotificationUtil;
import accommodation.booking.service.service.notification.NotificationService;
import accommodation.booking.service.service.notification.PaymentNotificationUtil;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class NotificationUtilTest {
    private static final Long VALID_ID = 1L;
    private static final BigDecimal AMOUNT_TO_PAY = BigDecimal.valueOf(200.00);
    private static final String URL = "http://localhost:8081/payments/success?session_id=123";
    private static final Accommodation.AccommodationType ACCOMMODATION_TYPE =
            Accommodation.AccommodationType.HOUSE;
    private static final String LOCATION = "City";
    private static final LocalDate IN_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate OUT_DATE = LocalDate.of(2025, 1, 10);
    private Accommodation accommodation;
    private Booking booking;

    private ListAppender<ILoggingEvent> logAppender;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentNotificationUtil paymentNotificationUtil;

    @InjectMocks
    private AccommodationNotificationUtil accommodationNotificationUtil;

    @InjectMocks
    private BookingNotificationUtil bookingNotificationUtil;

    @BeforeEach
    void setUp() {
        logAppender = new ListAppender<>();
        logAppender.start();
        Logger logger = (Logger) LoggerFactory.getLogger(PaymentNotificationUtil.class);
        logger.addAppender(logAppender);
        accommodation = new Accommodation();
        accommodation.setId(VALID_ID);
        booking = new Booking();
    }

    @AfterEach
    void tearDown() {
        logAppender.stop();
        logAppender.list.clear();
    }

    @Test
    @DisplayName("Notify payment created. Sends notification successfully")
    void notifyPaymentCreated_ValidInput_SendsNotification() {
        // Given: Valid input for notification
        String expectedMessage = String.format(
                "Payment initiated for Booking ID=%d: Amount=$%.2f, URL=%s",
                VALID_ID, AMOUNT_TO_PAY, URL);
        // When: Notify payment created
        paymentNotificationUtil.notifyPaymentCreated(VALID_ID, AMOUNT_TO_PAY, URL);

        // Then: Verify the notification was sent
        verify(notificationService).sendNotification(expectedMessage);
        assertThat(logAppender.list).isEmpty(); // No error logs
    }

    @Test
    @DisplayName("Notify payment created with notification error.")
    void notifyPaymentCreated_NotificationError_LogsError() {
        // Given: Notification service throws an exception
        String expectedMessage = String.format(
                "Payment initiated for Booking ID=%d: Amount=$%.2f, URL=%s",
                VALID_ID, AMOUNT_TO_PAY, URL);
        RuntimeException exception = new RuntimeException("Notification failed");
        doThrow(exception).when(notificationService).sendNotification(expectedMessage);

        // When: Notify payment created
        paymentNotificationUtil.notifyPaymentCreated(VALID_ID, AMOUNT_TO_PAY, URL);

        // Then: Verify the notification was attempted and error was logged
        verify(notificationService).sendNotification(expectedMessage);
        assertThat(logAppender.list).hasSize(1);
        ILoggingEvent logEvent = logAppender.list.getFirst();
        assertThat(logEvent.getFormattedMessage()).contains(
                "Error sending payment creation notification for Booking ID=" + VALID_ID);
        assertThat(logEvent.getThrowableProxy().getMessage()).isEqualTo("Notification failed");
    }

    @Test
    @DisplayName("Notify accommodation released. Sends notification successfully")
    void notifyAccommodationReleased_ValidInput_SendsNotification() {
        // Given: Valid input for notification
        accommodation.setType(ACCOMMODATION_TYPE);
        accommodation.setLocation(LOCATION);
        String expectedMessage = String.format(
                "Accommodation released: ID=%d, Type=%s, Location=%s",
                VALID_ID, ACCOMMODATION_TYPE, LOCATION);
        // When: Notify accommodation released
        accommodationNotificationUtil.notifyAccommodationReleased(accommodation);

        // Then: Verify the notification was sent
        verify(notificationService).sendNotification(expectedMessage);
        assertThat(logAppender.list).isEmpty(); // No error logs
    }

    @Test
    @DisplayName("Notify booking released. Sends notification successfully")
    void notifyBookingCancelled_ValidInput_SendsNotification() {
        // Given: Valid input for notification
        booking.setId(VALID_ID);
        booking.setCheckInDate(IN_DATE);
        booking.setCheckOutDate(OUT_DATE);
        String expectedMessage = String.format(
                "Booking canceled: ID=%d, Accommodation=%s, Dates=%s to %s",
                VALID_ID, VALID_ID, IN_DATE, OUT_DATE);
        // When: Notify booking released
        bookingNotificationUtil.notifyBookingCancelled(booking, accommodation);

        // Then: Verify the notification was sent
        verify(notificationService).sendNotification(expectedMessage);
        assertThat(logAppender.list).isEmpty(); // No error logs
    }
}
