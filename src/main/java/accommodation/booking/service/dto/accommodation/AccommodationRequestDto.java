package accommodation.booking.service.dto.accommodation;

import accommodation.booking.service.model.Accommodation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AccommodationRequestDto {
    @NotNull(message = "Accommodation type cannot be null.")
    private Accommodation.AccommodationType type;

    @NotBlank(message = "Location cannot be blank.")
    private String location;

    private String size;

    private List<String> amenities;

    @NotNull(message = "Daily rate cannot be null.")
    private BigDecimal dailyRate;

    @NotNull(message = "Availability cannot be null.")
    private Integer availability;
}


