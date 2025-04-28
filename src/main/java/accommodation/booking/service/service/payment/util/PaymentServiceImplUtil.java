package accommodation.booking.service.service.payment.util;

import static accommodation.booking.service.model.Role.RoleName.ROLE_MANAGER;

import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Payment;
import accommodation.booking.service.model.User;
import accommodation.booking.service.repository.BookingRepository;
import accommodation.booking.service.repository.PaymentRepository;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentServiceImplUtil {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public BigDecimal calculateAmountToPay(Booking booking) {
        long days = booking.getCheckInDate().until(booking.getCheckOutDate()).getDays();
        return booking.getAccommodation()
                .getDailyRate()
                .multiply(BigDecimal.valueOf(days));
    }

    public Payment initializePayment(Booking booking, Session session, BigDecimal amountToPay) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        payment.setAmountToPay(amountToPay);
        return payment;
    }

    public void checkoutAccessForUser(Long userId, User currentUser) {
        if (!currentUser.getId().equals(userId)
                && !currentUser.getRoles().contains(ROLE_MANAGER)) {
            throw new AccessDeniedException(
                    "You can only view your own payments unless you’re a manager.");
        }
    }

    public void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID is invalid or missing");
        }
    }

    public Payment retrievePayment(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found for session: " + sessionId));
    }

    public Booking retrieveBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Booking not found with ID: " + bookingId));
    }
}
