package accommodationbookingservice.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingStatusPatchRequestDto {
    @NotNull(message = "Status is required.")
    private String status;
}

