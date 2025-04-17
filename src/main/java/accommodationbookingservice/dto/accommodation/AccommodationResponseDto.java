package accommodationbookingservice.dto.accommodation;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AccommodationResponseDto {
    private Long id;
    private String type; // e.g., HOUSE, APARTMENT
    private String location;
    private String size;
    private List<String> amenities;
    private BigDecimal dailyRate;
    private Integer availability;
}

