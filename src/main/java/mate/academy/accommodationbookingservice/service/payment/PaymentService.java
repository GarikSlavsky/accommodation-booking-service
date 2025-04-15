package mate.academy.accommodationbookingservice.service.payment;

import com.stripe.exception.StripeException;
import java.util.List;
import mate.academy.accommodationbookingservice.dto.payment.PaymentResponseDto;
import mate.academy.accommodationbookingservice.model.User;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    List<PaymentResponseDto> getPaymentsForUser(Long userId, User currentUser, Pageable pageable);

    PaymentResponseDto initiatePayment(Long bookingId, User currentUser) throws StripeException;

    String handlePaymentSuccess(String sessionId);

    String handlePaymentCancel(String sessionId);

    PaymentResponseDto renewPaymentSession(Long paymentId, User currentUser) throws StripeException;
}
