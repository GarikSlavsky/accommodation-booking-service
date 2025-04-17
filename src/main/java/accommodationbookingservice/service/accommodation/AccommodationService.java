package accommodationbookingservice.service.accommodation;

import accommodationbookingservice.dto.accommodation.AccommodationRequestDto;
import accommodationbookingservice.dto.accommodation.AccommodationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccommodationService {
    AccommodationResponseDto addAccommodation(AccommodationRequestDto requestDto);

    AccommodationResponseDto getAccommodationById(Long id);

    Page<AccommodationResponseDto> getAllAccommodations(Pageable pageable);

    AccommodationResponseDto updateAccommodation(Long id, AccommodationRequestDto requestDto);

    void deleteAccommodation(Long id);
}
