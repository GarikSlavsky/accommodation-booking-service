package mate.academy.accommodationbookingservice.service.payment;

import java.util.List;
import mate.academy.accommodationbookingservice.dto.payment.PaymentResponseDto;
import mate.academy.accommodationbookingservice.model.User;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponseDto initiatePayment(Long bookingId, User currentUser);

    List<PaymentResponseDto> getPaymentsForUser(Long userId, User currentUser, Pageable pageable);

    PaymentResponseDto renewPaymentSession(Long paymentId, User currentUser);

    String handlePaymentSuccess(String sessionId);

    String handlePaymentCancel(String sessionId);
}
