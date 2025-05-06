package accommodation.booking.service.service.accommodation;

import accommodation.booking.service.dto.accommodation.AccommodationRequestDto;
import accommodation.booking.service.dto.accommodation.AccommodationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccommodationService {
    AccommodationResponseDto addAccommodation(AccommodationRequestDto requestDto);

    AccommodationResponseDto getAccommodationById(Long id);

    Page<AccommodationResponseDto> getAllAccommodations(Pageable pageable);

    AccommodationResponseDto updateAccommodation(Long id, AccommodationRequestDto requestDto);

    void deleteAccommodation(Long id);
}
