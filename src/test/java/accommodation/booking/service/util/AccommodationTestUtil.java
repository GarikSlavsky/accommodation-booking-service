package accommodation.booking.service.util;

import accommodation.booking.service.dto.accommodation.AccommodationRequestDto;
import accommodation.booking.service.dto.accommodation.AccommodationResponseDto;
import accommodation.booking.service.model.Accommodation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AccommodationTestUtil {
    private static final Long ACTUAL_ID = 15L;
    private static final BigDecimal INITIAL_DAILY_RATE = BigDecimal.valueOf(30.00);

    public static AccommodationRequestDto initializeAccommodationRequestDto() {
        AccommodationRequestDto dto = new AccommodationRequestDto();
        dto.setType(Accommodation.AccommodationType.APARTMENT);
        dto.setLocation("New York");
        dto.setSize("Standard");
        dto.setAmenities(new ArrayList<>(Arrays.asList("WiFi", "Pool")));
        dto.setDailyRate(INITIAL_DAILY_RATE);
        dto.setAvailability(10);
        return dto;
    }

    public static Accommodation initializeAccommodation(AccommodationRequestDto dto) {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(ACTUAL_ID);
        accommodation.setType(dto.getType());
        accommodation.setLocation(dto.getLocation());
        accommodation.setSize(dto.getSize());
        accommodation.setAmenities(dto.getAmenities());
        accommodation.setDailyRate(dto.getDailyRate());
        accommodation.setAvailability(dto.getAvailability());
        return accommodation;
    }

    public static AccommodationResponseDto initializeAccommodationResponseDto(
            Accommodation accommodation) {

        AccommodationResponseDto dto = new AccommodationResponseDto();
        dto.setId(accommodation.getId());
        dto.setType(accommodation.getType());
        dto.setLocation(accommodation.getLocation());
        dto.setSize(accommodation.getSize());
        dto.setAmenities(accommodation.getAmenities());
        dto.setDailyRate(accommodation.getDailyRate());
        dto.setAvailability(accommodation.getAvailability());
        return dto;
    }

    public static List<AccommodationResponseDto> getAccommodationResponseDtoList() {
        AccommodationRequestDto requestDto =
                AccommodationTestUtil.initializeAccommodationRequestDto();
        Accommodation accommodation = AccommodationTestUtil.initializeAccommodation(requestDto);
        final AccommodationResponseDto dto1 =
                AccommodationTestUtil.initializeAccommodationResponseDto(accommodation);

        final AccommodationResponseDto dto2 = new AccommodationResponseDto();
        dto2.setId(18L);
        dto2.setType(Accommodation.AccommodationType.CONDO);
        dto2.setLocation("Miami");
        dto2.setSize("Standard");
        dto2.setAmenities(new ArrayList<>(Arrays.asList("Dining", "Parking")));
        dto2.setDailyRate(BigDecimal.valueOf(140.00));
        dto2.setAvailability(10);

        List<AccommodationResponseDto> responseDtoList = new ArrayList<>();
        responseDtoList.add(dto1);
        responseDtoList.add(dto2);

        return responseDtoList;
    }
}
