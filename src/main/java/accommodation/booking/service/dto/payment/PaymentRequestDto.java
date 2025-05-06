package accommodation.booking.service.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDto {
    @NotNull
    private Long bookingId;
}
