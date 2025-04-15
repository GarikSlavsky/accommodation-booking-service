package mate.academy.accommodationbookingservice.service.payment;

import java.math.BigDecimal;
import com.stripe.model.checkout.Session;
import mate.academy.accommodationbookingservice.model.Booking;
import mate.academy.accommodationbookingservice.model.Payment;

public interface StripeService {
    Session createSession(Booking booking, BigDecimal amountToPay);

    Session retrieveSession(String sessionId);

    boolean isSessionExpired(Payment payment);
}
