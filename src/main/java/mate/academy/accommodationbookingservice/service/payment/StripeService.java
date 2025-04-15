package mate.academy.accommodationbookingservice.service.payment;

import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import mate.academy.accommodationbookingservice.model.Booking;
import mate.academy.accommodationbookingservice.model.Payment;

public interface StripeService {
    Session createSession(Booking booking, BigDecimal amountToPay);

    Session retrieveSession(String sessionId);

    boolean isSessionExpired(Payment payment);
}
