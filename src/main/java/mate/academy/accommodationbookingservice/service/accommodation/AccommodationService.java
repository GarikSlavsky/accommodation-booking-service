package mate.academy.accommodationbookingservice.service.accommodation;

import java.util.List;
import mate.academy.accommodationbookingservice.dto.accommodation.AccommodationRequestDto;
import mate.academy.accommodationbookingservice.dto.accommodation.AccommodationResponseDto;
import org.springframework.data.domain.Pageable;

public interface AccommodationService {
    AccommodationResponseDto addAccommodation(AccommodationRequestDto requestDto);

    AccommodationResponseDto getAccommodationById(Long id);

    List<AccommodationResponseDto> getAllAccommodations(Pageable pageable);

    AccommodationResponseDto updateAccommodation(Long id, AccommodationRequestDto requestDto);

    void deleteAccommodation(Long id);
}
