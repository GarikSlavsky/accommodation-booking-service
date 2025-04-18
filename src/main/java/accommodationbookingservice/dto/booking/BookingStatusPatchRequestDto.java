package accommodationbookingservice.dto.booking;

import accommodationbookingservice.model.Booking;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingStatusPatchRequestDto {
    @NotNull(message = "Status is required.")
    private Booking.BookingStatus status;
}

