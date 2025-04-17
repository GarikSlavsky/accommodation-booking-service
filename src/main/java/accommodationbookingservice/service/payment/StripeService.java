package accommodationbookingservice.service.payment;

import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import accommodationbookingservice.model.Booking;
import accommodationbookingservice.model.Payment;

public interface StripeService {
    Session createSession(Booking booking, BigDecimal amountToPay);

    Session retrieveSession(String sessionId);

    boolean isSessionExpired(Payment payment);
}
