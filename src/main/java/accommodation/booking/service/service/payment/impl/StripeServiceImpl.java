package accommodation.booking.service.service.payment.impl;

import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Payment;
import accommodation.booking.service.service.payment.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {
    private static final Logger logger = LoggerFactory.getLogger(StripeServiceImpl.class);
    private static final String SUCCESS_PATH = "/payments/success";
    private static final String CANCEL_PATH = "/payments/cancel";
    private static final String URL_QUERY_PARAM = "?session_id={CHECKOUT_SESSION_ID}";

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
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
        return UriComponentsBuilder.fromUriString("http://localhost:8081")
                .path(path)
                .toUriString();
    }
}
