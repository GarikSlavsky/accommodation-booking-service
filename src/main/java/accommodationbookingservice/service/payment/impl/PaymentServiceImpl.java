package accommodationbookingservice.service.payment.impl;

import accommodationbookingservice.dto.payment.PaymentResponseDto;
import accommodationbookingservice.mapper.PaymentMapper;
import accommodationbookingservice.model.Booking;
import accommodationbookingservice.model.Payment;
import accommodationbookingservice.model.User;
import accommodationbookingservice.repository.BookingRepository;
import accommodationbookingservice.repository.PaymentRepository;
import accommodationbookingservice.service.notification.NotificationService;
import accommodationbookingservice.service.payment.PaymentService;
import accommodationbookingservice.service.payment.StripeService;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponseDto initiatePayment(Long bookingId, User currentUser) {
        Booking booking = retrieveBookingById(bookingId);
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only initiate payment for your own bookings.");
        }

        if (paymentRepository.findByBooking(booking).isPresent()) {
            throw new IllegalStateException("Payment already exists for this booking.");
        }

        BigDecimal amountToPay = calculateAmountToPay(booking);
        Session session = stripeService.createSession(booking, amountToPay);
        Payment payment = initializePayment(booking, session, amountToPay);
        Payment savedPayment = paymentRepository.save(payment);
        notificationService.sendNotification(
                String.format("Payment initiated for Booking ID=%d: Amount=$%.2f, URL=%s",
                        bookingId, amountToPay, session.getUrl())
        );

        return paymentMapper.intoDto(savedPayment);
    }

    @Override
    public List<PaymentResponseDto> getPaymentsForUser(
            Long userId, User currentUser, Pageable pageable) {

        checkoutAccessForUser(userId, currentUser);
        List<Payment> payments = currentUser.getAuthorities().contains("ROLE_MANAGER")
                ? paymentRepository.findAll()
                : paymentRepository.findAllByBookingUserId(userId);

        return payments.stream()
                .map(paymentMapper::intoDto)
                .toList();
    }

    @Scheduled(fixedRate = 60000)
    public void checkExpiredStripeSessions() {
        List<Payment> pendingPayments =
                paymentRepository.findAllByStatus(Payment.PaymentStatus.PENDING);
        logger.info("Checking {} pending payments for expiration", pendingPayments.size());
        for (Payment payment : pendingPayments) {
            if (stripeService.isSessionExpired(payment)) {
                payment.setStatus(Payment.PaymentStatus.EXPIRED);
                paymentRepository.save(payment);
                notificationService.sendNotification(
                        String.format("Payment session expired for Booking ID=%d: Amount=$%.2f",
                                payment.getBooking().getId(), payment.getAmountToPay())
                );
                logger.info("Payment ID={} marked as EXPIRED for Booking ID={}",
                        payment.getId(), payment.getBooking().getId());
            }
        }
    }

    @Override
    public PaymentResponseDto renewPaymentSession(Long paymentId, User currentUser) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found with ID: " + paymentId));

        if (!payment.getBooking().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only renew your own payments.");
        }

        if (payment.getStatus() != Payment.PaymentStatus.EXPIRED) {
            throw new IllegalStateException("Only EXPIRED payments can be renewed.");
        }

        Booking booking = retrieveBookingById(payment.getBooking().getId());
        BigDecimal amountToPay = calculateAmountToPay(booking);
        Session session = stripeService.createSession(booking, amountToPay);
        System.out.println(session.getPaymentStatus());
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        Payment updatedPayment = paymentRepository.save(payment);

        notificationService.sendNotification(
                String.format("Payment session renewed for Booking ID=%d: Amount=$%.2f, URL=%s",
                        booking.getId(), amountToPay, session.getUrl())
        );
        return paymentMapper.intoDto(updatedPayment);
    }

    @Override
    public String handlePaymentSuccess(String sessionId) {
        validateSessionId(sessionId);
        Session session = stripeService.retrieveSession(sessionId);
        Payment payment = retrievePayment(sessionId);
        System.out.println(session.getPaymentStatus());

        if ("paid".equals(session.getPaymentStatus())) {
            payment.setStatus(Payment.PaymentStatus.PAID);
            paymentRepository.save(payment);
            notificationService.sendNotification(String.format(
                    "Payment successful for Booking ID=%d: Amount=$%.2f, Session ID=%s",
                            payment.getBooking().getId(), payment.getAmountToPay(), sessionId)
            );
            return "Payment successful! Booking confirmed.";
        }
        return "Payment status pending. Please check later.";
    }

    @Override
    public String handlePaymentCancel(String sessionId) {
        validateSessionId(sessionId);
        Payment payment = retrievePayment(sessionId);
        notificationService.sendNotification(
                String.format("Payment canceled for Booking ID=%d: Amount=$%.2f",
                        payment.getBooking().getId(), payment.getAmountToPay())
        );
        return "Payment canceled. You can try again later.";
    }

    private BigDecimal calculateAmountToPay(Booking booking) {
        long days = booking.getCheckInDate().until(booking.getCheckOutDate()).getDays();
        return booking.getAccommodation()
                .getDailyRate()
                .multiply(BigDecimal.valueOf(days));
    }

    private Payment initializePayment(Booking booking, Session session, BigDecimal amountToPay) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        payment.setAmountToPay(amountToPay);
        return payment;
    }

    private void checkoutAccessForUser(Long userId, User currentUser) {
        if (!currentUser.getId().equals(userId)
                && !currentUser.getAuthorities().contains("ROLE_MANAGER")) {
            throw new AccessDeniedException(
                    "You can only view your own payments unless you’re a manager.");
        }
    }

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID is invalid or missing");
        }
    }

    private Payment retrievePayment(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found for session: " + sessionId));
    }

    private Booking retrieveBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Booking not found with ID: " + bookingId));
    }
}
