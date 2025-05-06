package accommodation.booking.service.dto.booking;

import accommodation.booking.service.validation.bookingdate.CheckInBeforeCheckOut;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
@CheckInBeforeCheckOut
public class BookingRequestDto {
    @NotNull(message = "Accommodation ID is required.")
    private Long accommodationId;

    @NotNull(message = "Check-in date is required.")
    @FutureOrPresent(message = "Check-in date must be today or in the future.")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required.")
    @FutureOrPresent(message = "Check-out date must be today or in the future.")
    private LocalDate checkOutDate;
}
