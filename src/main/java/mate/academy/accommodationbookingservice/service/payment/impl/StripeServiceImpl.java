package mate.academy.accommodationbookingservice.service.payment.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import mate.academy.accommodationbookingservice.model.Booking;
import mate.academy.accommodationbookingservice.model.Payment;
import mate.academy.accommodationbookingservice.service.payment.StripeService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {
    private static final Logger logger = LoggerFactory.getLogger(StripeServiceImpl.class);
    private static final String SUCCESS_PATH = "/payments/success";
    private static final String CANCEL_PATH = "/payments/cancel";
    private static final String URL_QUERY_PARAM = "?session_id={CHECKOUT_SESSION_ID}";

    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        Stripe.apiKey = dotenv.get("STRIPE_SECRET_KEY");
    }

    @Override
    public Session createSession(Booking booking, BigDecimal amountToPay) {
        SessionCreateParams params = getSessionCreateParams(amountToPay, booking.getId());
        try {
            return Session.create(params);
        } catch (StripeException e) {
            logger.error("Failed to create payment session for Booking ID={}: {}",
                    booking.getId(), e.getMessage());
            throw new RuntimeException("Error creating payment session for Booking ID: "
                    + booking.getId(), e);
        }
    }

    @Override
    public Session retrieveSession(String sessionId) {
        try {
            return Session.retrieve(sessionId);
        } catch (StripeException e) {
            logger.error("Failed to retrieve payment session: {}", sessionId, e);
            throw new RuntimeException(
                    "Unable to process payment at this time. Please try again later.");
        }
    }

    @Override
    public boolean isSessionExpired(Payment payment) {
        try {
            Session session = Session.retrieve(payment.getSessionId());
            if (session.getExpiresAt() != null) {
                return session.getExpiresAt() < System.currentTimeMillis() / 1000;
            }
            return false;
        } catch (StripeException e) {
            logger.error("Failed to check Stripe session for Payment ID={}: {}",
                    payment.getId(), e.getMessage());
            throw new RuntimeException("Stripe API error while checking session for Payment ID="
                    + payment.getId() + ": " + e.getMessage(), e);
        }
    }

    private SessionCreateParams getSessionCreateParams(BigDecimal amountToPay, Long bookingId) {
        return SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(buildUrl(SUCCESS_PATH) + URL_QUERY_PARAM)
                .setCancelUrl(buildUrl(CANCEL_PATH) + URL_QUERY_PARAM)
                .addLineItem(SessionCreateParams.LineItem
                        .builder()
                        .setPriceData(SessionCreateParams.LineItem
                                .PriceData
                                .builder()
                                .setCurrency("usd")
                                .setUnitAmount(amountToPay.multiply(
                                        BigDecimal.valueOf(100)).longValue())
                                .setProductData(SessionCreateParams.LineItem
                                        .PriceData
                                        .ProductData
                                        .builder()
                                        .setName("Booking #" + bookingId)
                                        .build())
                                .build())
                        .setQuantity(1L)
                        .build())
                .build();
    }

    private String buildUrl(String path) {
        return UriComponentsBuilder.fromUriString("http://localhost:8080")
                .path(path)
                .toUriString();
    }
}
