package accommodation.booking.service.dto.booking;

import accommodation.booking.service.model.Booking;
import java.time.LocalDate;
import lombok.Data;

@Data
public class BookingResponseDto {
    private Long id;
    private Long accommodationId;
    private Long userId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Booking.BookingStatus status;
}

