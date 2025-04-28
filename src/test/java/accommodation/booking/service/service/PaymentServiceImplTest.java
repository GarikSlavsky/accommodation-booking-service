package accommodation.booking.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import accommodation.booking.service.dto.payment.PaymentResponseDto;
import accommodation.booking.service.mapper.PaymentMapper;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Payment;
import accommodation.booking.service.model.Role;
import accommodation.booking.service.model.User;
import accommodation.booking.service.repository.BookingRepository;
import accommodation.booking.service.repository.PaymentRepository;
import accommodation.booking.service.service.notification.PaymentNotificationUtil;
import accommodation.booking.service.service.payment.StripeService;
import accommodation.booking.service.service.payment.impl.PaymentServiceImpl;
import accommodation.booking.service.service.payment.util.PaymentServiceImplUtil;
import accommodation.booking.service.util.PaymentServiceTestUtil;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {
    private static final Long VALID_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final String SESSION_ID = "session_123";
    private static final String PAID_PAYMENT_STATUS = "paid";
    private Booking booking;
    private User currentUser;
    private Role role;
    private Payment payment;
    private PaymentResponseDto responseDto;
    private Session stripeSession;
    private Pageable pageable;
    private BigDecimal amount;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentNotificationUtil paymentNotificationUtil;

    @Mock
    private PaymentServiceImplUtil paymentServiceImplUtil;

    @BeforeEach
    void setUp() {
        PaymentServiceTestUtil paymentServiceTestUtil =
                new PaymentServiceTestUtil(paymentServiceImplUtil);
        role = paymentServiceTestUtil.initializeRole();
        currentUser = paymentServiceTestUtil.initializeUser(role);
        booking = paymentServiceTestUtil.initializeBooking(currentUser);
        payment = paymentServiceTestUtil.initializePayment(booking);
        responseDto = paymentServiceTestUtil.initializePaymentResponseDto(payment);
        stripeSession = paymentServiceTestUtil.initializeStripeSession();
        amount = paymentServiceImplUtil.calculateAmountToPay(booking);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Initiate payment for valid booking creates payment and returns response DTO")
    void initiatePayment_ValidBooking_ReturnsPaymentResponseDto() {
        // Given: Valid booking with no existing payment
        when(bookingRepository.findBookingByIdAndUserId(VALID_ID, VALID_ID))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());
        when(stripeService.createSession(booking, amount))
                .thenReturn(stripeSession);
        when(paymentServiceImplUtil.initializePayment(booking, stripeSession, amount))
                .thenReturn(any(Payment.class));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.intoDto(payment)).thenReturn(responseDto);

        // When: Call the service method
        PaymentResponseDto actual = paymentService.initiatePayment(VALID_ID, currentUser);

        // Then: Verify the result and interactions
        assertThat(actual).isEqualTo(responseDto);
        assertThat(actual.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        assertThat(actual.getAmountToPay()).isEqualTo(amount);
        verify(bookingRepository).findBookingByIdAndUserId(VALID_ID, VALID_ID);
        verify(paymentRepository).findByBooking(booking);
        verify(stripeService).createSession(booking, amount);
        verify(paymentMapper).intoDto(payment);
    }

    @Test
    @DisplayName("Initiate payment for booking with existing payment.")
    void initiatePayment_ExistingPayment_ThrowsIllegalStateException() {
        // Given: Booking has an existing payment
        when(bookingRepository.findBookingByIdAndUserId(VALID_ID, VALID_ID))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));

        // When/Then: Verify that an exception is thrown
        assertThrows(IllegalStateException.class,
                () -> paymentService.initiatePayment(VALID_ID, currentUser));

        // Verify: No further interactions occur after the exception
        verify(bookingRepository).findBookingByIdAndUserId(VALID_ID, VALID_ID);
        verify(paymentRepository).findByBooking(booking);
        verify(stripeService, never()).createSession(booking, amount);
        verify(paymentRepository, never()).save(payment);
        verify(paymentMapper, never()).intoDto(payment);
    }

    @Test
    @DisplayName("Get payments for own user ID.")
    void getPaymentsForUser_OwnUserId_ReturnsPaymentPage() {
        // Given: User requests their own payments
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment), pageable, 1);
        when(paymentRepository.findAllByBookingUserId(VALID_ID, pageable)).thenReturn(paymentPage);
        when(paymentMapper.intoDto(payment)).thenReturn(responseDto);

        // When: Call the service method
        Page<PaymentResponseDto> actual =
                paymentService.getPaymentsForUser(VALID_ID, currentUser, pageable);

        // Then: Verify the result and interactions
        assertThat(actual.getContent()).hasSize(1);
        assertThat(actual.getContent().getFirst()).isEqualTo(responseDto);
        verify(paymentRepository).findAllByBookingUserId(VALID_ID, pageable);
        verify(paymentMapper).intoDto(payment);
    }

    @Test
    @DisplayName("Get payments for another user ID without manager role.")
    void getPaymentsForUser_UnauthorizedUserId_ThrowsAccessDeniedException() {
        // Given: Non-manager user requests another user's payments
        doCallRealMethod().when(paymentServiceImplUtil).
                checkoutAccessForUser(OTHER_USER_ID, currentUser);
        // When/Then: Verify that an exception is thrown
        assertThrows(AccessDeniedException.class,
                () -> paymentService.getPaymentsForUser(OTHER_USER_ID, currentUser, pageable));

        // Verify: No repository or mapper interactions occur
        verify(paymentRepository, never()).findAllByBookingUserId(VALID_ID, pageable);
        verify(paymentRepository, never()).findAll(pageable);
        verify(paymentMapper, never()).intoDto(payment);
    }

    @Test
    @DisplayName("Check expired Stripe sessions marks expired payment and sends notification.")
    void checkExpiredStripeSessions_ExpiredSession_MarksExpiredAndNotifies() {
        // Given: One pending payment with an expired session
        when(paymentRepository.findAllByStatus(Payment.PaymentStatus.PENDING))
                .thenReturn(List.of(payment));
        when(stripeService.isSessionExpired(payment)).thenReturn(true);
        when(paymentRepository.save(payment)).thenReturn(payment);

        // When: Call the service method
        paymentService.checkExpiredStripeSessions();

        // Then: Verify the payment is marked EXPIRED and notification is sent
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.EXPIRED);
        verify(paymentRepository).findAllByStatus(Payment.PaymentStatus.PENDING);
        verify(stripeService).isSessionExpired(payment);
        verify(paymentRepository).save(payment);
        verify(paymentNotificationUtil).notifyOfExpiredSession(payment);
    }

    @Test
    @DisplayName("Handle payment success with paid status updates payment and returns success message")
    void handlePaymentSuccess_PaidStatus_UpdatesPaymentAndReturnsSuccessMessage() {
        // Given: Valid session ID with paid status
        stripeSession.setPaymentStatus(PAID_PAYMENT_STATUS);
        when(stripeService.retrieveSession(SESSION_ID)).thenReturn(stripeSession);
        when(paymentServiceImplUtil.retrievePayment(SESSION_ID)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);

        // When: Call the service method
        String result = paymentService.handlePaymentSuccess(SESSION_ID);

        // Then: Verify the result and interactions
        assertThat(result).isEqualTo("Payment successful! Booking confirmed.");
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.PAID);
        verify(stripeService).retrieveSession(SESSION_ID);
        verify(paymentRepository).save(payment);
        verify(paymentNotificationUtil).notifyOfSuccessfulPayment(payment, SESSION_ID);
    }
}
