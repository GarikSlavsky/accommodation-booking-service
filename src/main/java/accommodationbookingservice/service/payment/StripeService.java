package accommodationbookingservice.service.payment;

import accommodationbookingservice.model.Booking;
import accommodationbookingservice.model.Payment;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;

public interface StripeService {
    Session createSession(Booking booking, BigDecimal amountToPay);

    Session retrieveSession(String sessionId);

    boolean isSessionExpired(Payment payment);
}
