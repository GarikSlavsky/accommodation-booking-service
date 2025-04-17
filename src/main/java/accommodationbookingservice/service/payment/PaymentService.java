package accommodationbookingservice.service.payment;

import accommodationbookingservice.dto.payment.PaymentResponseDto;
import accommodationbookingservice.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponseDto initiatePayment(Long bookingId, User currentUser);

    List<PaymentResponseDto> getPaymentsForUser(Long userId, User currentUser, Pageable pageable);

    PaymentResponseDto renewPaymentSession(Long paymentId, User currentUser);

    String handlePaymentSuccess(String sessionId);

    String handlePaymentCancel(String sessionId);
}
