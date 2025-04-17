package accommodationbookingservice.dto.payment;

import accommodationbookingservice.model.Payment;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentResponseDto {
    private Long id;
    private Long bookingId;
    private Payment.PaymentStatus status;
    private String sessionUrl;
    private String sessionId;
    private BigDecimal amountToPay;
}
