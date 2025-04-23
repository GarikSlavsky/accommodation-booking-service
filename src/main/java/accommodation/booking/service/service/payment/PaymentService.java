package accommodation.booking.service.service.payment;

import accommodation.booking.service.dto.payment.PaymentResponseDto;
import accommodation.booking.service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponseDto initiatePayment(Long bookingId, User currentUser);

    Page<PaymentResponseDto> getPaymentsForUser(Long userId, User currentUser, Pageable pageable);

    PaymentResponseDto renewPaymentSession(Long paymentId, User currentUser);

    String handlePaymentSuccess(String sessionId);

    String handlePaymentCancel(String sessionId);
}
