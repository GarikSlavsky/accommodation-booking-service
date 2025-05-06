package accommodation.booking.service.service.payment;

import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Payment;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;

public interface StripeService {
    Session createSession(Booking booking, BigDecimal amountToPay);

    Session retrieveSession(String sessionId);

    boolean isSessionExpired(Payment payment);
}
