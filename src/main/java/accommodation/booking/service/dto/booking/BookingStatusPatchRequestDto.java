package accommodation.booking.service.dto.booking;

import accommodation.booking.service.model.Booking;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingStatusPatchRequestDto {
    @NotNull(message = "Status is required.")
    private Booking.BookingStatus status;
}

