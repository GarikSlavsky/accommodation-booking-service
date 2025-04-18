package accommodationbookingservice.service.payment;

import accommodationbookingservice.dto.payment.PaymentResponseDto;
import accommodationbookingservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponseDto initiatePayment(Long bookingId, User currentUser);

    Page<PaymentResponseDto> getPaymentsForUser(Long userId, User currentUser, Pageable pageable);

    PaymentResponseDto renewPaymentSession(Long paymentId, User currentUser);

    String handlePaymentSuccess(String sessionId);

    String handlePaymentCancel(String sessionId);
}
