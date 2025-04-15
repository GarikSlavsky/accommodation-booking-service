package mate.academy.accommodationbookingservice.dto.payment;

import java.math.BigDecimal;
import lombok.Data;
import mate.academy.accommodationbookingservice.model.Payment;

@Data
public class PaymentResponseDto {
    private Long id;
    private Long bookingId;
    private Payment.PaymentStatus status;
    private String sessionUrl;
    private String sessionId;
    private BigDecimal amountToPay;
}
