package accommodation.booking.service.service.payment.impl;

import static accommodation.booking.service.model.Role.RoleName.ROLE_MANAGER;

import accommodation.booking.service.dto.payment.PaymentResponseDto;
import accommodation.booking.service.mapper.PaymentMapper;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Payment;
import accommodation.booking.service.model.User;
import accommodation.booking.service.repository.BookingRepository;
import accommodation.booking.service.repository.PaymentRepository;
import accommodation.booking.service.service.notification.PaymentNotificationUtil;
import accommodation.booking.service.service.payment.PaymentService;
import accommodation.booking.service.service.payment.StripeService;
import accommodation.booking.service.service.payment.util.PaymentServiceImplUtil;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final String PAID_PAYMENT_STATUS = "paid";
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;
    private final PaymentNotificationUtil paymentNotificationUtil;
    private final PaymentServiceImplUtil paymentServiceImplUtil;

    @Transactional
    @Override
    public PaymentResponseDto initiatePayment(Long bookingId, User currentUser) {
        Booking booking = bookingRepository.findBookingByIdAndUserId(bookingId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: "
                        + bookingId
                        + " or does not belong to user with ID: " + currentUser.getId()));

        if (paymentRepository.findByBooking(booking).isPresent()) {
            throw new IllegalStateException("Payment already exists for this booking.");
        }

        BigDecimal amountToPay = paymentServiceImplUtil.calculateAmountToPay(booking);
        Session session = stripeService.createSession(booking, amountToPay);
        Payment payment = paymentServiceImplUtil.initializePayment(booking, session, amountToPay);
        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponseDto responseDto = paymentMapper.intoDto(savedPayment);
        paymentNotificationUtil.notifyPaymentCreated(bookingId, amountToPay, session.getUrl());
        return responseDto;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PaymentResponseDto> getPaymentsForUser(
            Long userId, User currentUser, Pageable pageable) {

        paymentServiceImplUtil.checkoutAccessForUser(userId, currentUser);
        Page<Payment> payments = currentUser.getRoles().contains(ROLE_MANAGER)
                ? paymentRepository.findAll(pageable)
                : paymentRepository.findAllByBookingUserId(userId, pageable);
        return payments.map(paymentMapper::intoDto);
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void checkExpiredStripeSessions() {
        List<Payment> pendingPayments = paymentRepository.findAllByStatus(
                Payment.PaymentStatus.PENDING);
        logger.info("Checking {} pending payments for expiration", pendingPayments.size());

        for (Payment payment : pendingPayments) {
            try {
                if (stripeService.isSessionExpired(payment)) {
                    payment.setStatus(Payment.PaymentStatus.EXPIRED);
                    paymentRepository.save(payment);
                    logger.info("Payment ID={} marked as EXPIRED for Booking ID={}",
                            payment.getId(), payment.getBooking().getId());
                    paymentNotificationUtil.notifyOfExpiredSession(payment);
                }
            } catch (Exception e) {
                logger.error("Failed to process payment ID={} for Booking ID={}: {}",
                        payment.getId(), payment.getBooking().getId(), e.getMessage());
            }
        }
    }

    @Transactional
    @Override
    public PaymentResponseDto renewPaymentSession(Long paymentId, User currentUser) {
        Payment payment = paymentRepository.findByIdAndUserIdAndStatus(
                        paymentId,
                        currentUser.getId(),
                        Payment.PaymentStatus.EXPIRED)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found, not owned by user, or not EXPIRED for ID: "
                                + paymentId));

        Booking booking = paymentServiceImplUtil.retrieveBookingById(payment.getBooking().getId());
        BigDecimal amountToPay = paymentServiceImplUtil.calculateAmountToPay(booking);
        Session session = stripeService.createSession(booking, amountToPay);

        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        Payment updatedPayment = paymentRepository.save(payment);

        paymentNotificationUtil.notifyOfSessionRenewed(
                booking.getId(), amountToPay, session.getUrl());
        return paymentMapper.intoDto(updatedPayment);
    }

    @Transactional
    @Override
    public String handlePaymentSuccess(String sessionId) {
        logger.info("Processing payment success for sessionId: {}", sessionId);
        paymentServiceImplUtil.validateSessionId(sessionId);
        Session session = stripeService.retrieveSession(sessionId);
        Payment payment = paymentServiceImplUtil.retrievePayment(sessionId);

        if (PAID_PAYMENT_STATUS.equals(session.getPaymentStatus())) {
            logger.info("Payment status is PAID for sessionId: {}", sessionId);
            payment.setStatus(Payment.PaymentStatus.PAID);
            paymentRepository.save(payment);
            logger.info("Payment processed successfully for sessionId: {}", sessionId);
            paymentNotificationUtil.notifyOfSuccessfulPayment(payment, sessionId);
            return "Payment successful! Booking confirmed.";
        } else {
            logger.warn("Payment status is not PAID for sessionId: {}. Current status: {}",
                    sessionId, session.getPaymentStatus());
            return "Payment status pending. Please check later.";
        }
    }

    @Transactional(readOnly = true)
    @Override
    public String handlePaymentCancel(String sessionId) {
        paymentServiceImplUtil.validateSessionId(sessionId);
        Payment payment = paymentServiceImplUtil.retrievePayment(sessionId);
        paymentNotificationUtil.notifyCancelledPayment(payment);
        return "Payment canceled. You can try again later.";
    }
}
