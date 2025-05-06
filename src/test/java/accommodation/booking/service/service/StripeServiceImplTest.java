package accommodation.booking.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.User;
import accommodation.booking.service.service.payment.impl.StripeServiceImpl;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class StripeServiceImplTest {
    private static final Long PROPER_ID = 1L;
    private static final BigDecimal AMOUNT_TO_PAY = BigDecimal.valueOf(200.00);
    private static final String SESSION_ID = "session_123";
    private static final String SESSION_URL = "http://localhost:8081/payments/success?session_id={CHECKOUT_SESSION_ID}";
    private Booking booking;
    @InjectMocks
    private StripeServiceImpl stripeService;

    @Mock
    private Session session;

    @BeforeEach
    void setUp() {
        booking = initializeBooking();
        ReflectionTestUtils.setField(stripeService, "secretKey", "test_secret_key");
    }

    @Test
    @DisplayName("Create session for valid booking.")
    void createSession_ValidBooking_ReturnsStripeSession() {
        // Given: Valid booking and amount to pay
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getUrl()).thenReturn(SESSION_URL);

        try (MockedStatic<Session> sessionMockedStatic = mockStatic(Session.class)) {
            sessionMockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(session);
            // When: Call the service method
            Session actual = stripeService.createSession(booking, AMOUNT_TO_PAY);

            // Then: Verify the result
            assertThat(actual).isEqualTo(session);
            assertThat(actual.getId()).isEqualTo(SESSION_ID);
            assertThat(actual.getUrl()).isEqualTo(SESSION_URL);
        }
    }

    @Test
    @DisplayName("Create session with Stripe API error.")
    void createSession_StripeApiError_ThrowsRuntimeException() {
        // Given: Stripe API throws an exception
        StripeException stripeException = new StripeException(
                "API error", "request_123", "account_number_invalid", 400) {};
        try (MockedStatic<Session> sessionMockedStatic = mockStatic(Session.class)) {
            sessionMockedStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(stripeException);
            // When/Then: Verify that an exception is thrown
            RuntimeException thrown = assertThrows(RuntimeException.class,
                    () -> stripeService.createSession(booking, AMOUNT_TO_PAY));

            // Then: Verify the exception message
            assertThat(thrown.getMessage()).contains(
                    "Error creating payment session for Booking ID: " + PROPER_ID);
            assertThat(thrown.getCause()).isEqualTo(stripeException);
        }
    }

    private Booking initializeBooking() {
        Booking booking = new Booking();
        booking.setId(PROPER_ID);
        booking.setUser(initializeUser());
        booking.setAccommodation(initializeAccommodation());
        booking.setCheckInDate(LocalDate.of(2025, 5, 1));
        booking.setCheckOutDate(LocalDate.of(2025, 5, 3));
        booking.setStatus(Booking.BookingStatus.PENDING);
        return booking;
    }

    private User initializeUser() {
        User user = new User();
        user.setId(PROPER_ID);
        return user;
    }

    private Accommodation initializeAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(PROPER_ID);
        accommodation.setDailyRate(BigDecimal.valueOf(100.00));
        return accommodation;
    }
}
