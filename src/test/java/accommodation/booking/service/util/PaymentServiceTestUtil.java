package accommodation.booking.service.util;

import accommodation.booking.service.dto.payment.PaymentResponseDto;
import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Payment;
import accommodation.booking.service.model.Role;
import accommodation.booking.service.model.User;
import accommodation.booking.service.service.payment.util.PaymentServiceImplUtil;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public final class PaymentServiceTestUtil {
    private static final Role.RoleName CUSTOMER = Role.RoleName.ROLE_CUSTOMER;
    private static final BigDecimal DAILY_RATE = BigDecimal.valueOf(100.00);
    private static final String SESSION_URL = "http://localhost:8081/payments/success?session_id=session_123";
    private static final String SESSION_ID = "session_123";
    private static final Long VALID_ID = 1L;

    public static User initializeUser(Role role) {
        User user = new User();
        user.setId(VALID_ID);
        user.setRoles(Set.of(role));
        return user;
    }

    public static Role initializeRole() {
        Role role = new Role();
        role.setId(VALID_ID);
        role.setName(CUSTOMER);
        return role;
    }

    public static Booking initializeBooking(User user) {
        Booking booking = new Booking();
        booking.setId(VALID_ID);
        booking.setUser(user);
        booking.setAccommodation(initializeAccommodation());
        booking.setCheckInDate(LocalDate.of(2025, 5, 1));
        booking.setCheckOutDate(LocalDate.of(2025, 5, 3));
        booking.setStatus(Booking.BookingStatus.PENDING);
        return booking;
    }

    public static Accommodation initializeAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(VALID_ID);
        accommodation.setDailyRate(DAILY_RATE);
        return accommodation;
    }

    public static Payment initializePayment(Booking booking) {
        Payment payment = new Payment();
        payment.setId(VALID_ID);
        payment.setBooking(booking);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setSessionUrl(SESSION_URL);
        payment.setSessionId(SESSION_ID);
        payment.setAmountToPay(PaymentServiceImplUtil.calculateAmountToPay(booking));
        return payment;
    }

    public static PaymentResponseDto initializePaymentResponseDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setBookingId(payment.getBooking().getId());
        dto.setStatus(payment.getStatus());
        dto.setSessionUrl(payment.getSessionUrl());
        dto.setSessionId(payment.getSessionId());
        dto.setAmountToPay(payment.getAmountToPay());
        return dto;
    }

    public static Session initializeStripeSession() {
        Session session = new Session();
        session.setId(SESSION_ID);
        session.setUrl(SESSION_URL);
        return session;
    }
}
