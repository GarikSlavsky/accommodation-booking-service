package accommodation.booking.service.dto.accommodation;

import accommodation.booking.service.model.Accommodation;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AccommodationResponseDto {
    private Long id;
    private Accommodation.AccommodationType type;
    private String location;
    private String size;
    private List<String> amenities;
    private BigDecimal dailyRate;
    private Integer availability;
}

