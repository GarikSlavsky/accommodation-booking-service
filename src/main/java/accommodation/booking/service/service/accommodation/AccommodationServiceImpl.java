package accommodation.booking.service.service.accommodation;

import accommodation.booking.service.dto.accommodation.AccommodationRequestDto;
import accommodation.booking.service.dto.accommodation.AccommodationResponseDto;
import accommodation.booking.service.mapper.AccommodationMapper;
import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.repository.AccommodationRepository;
import accommodation.booking.service.service.notification.AccommodationNotificationUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final AccommodationNotificationUtil accommodationNotificationUtil;

    @Transactional
    @Override
    public AccommodationResponseDto addAccommodation(AccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationMapper.intoModel(requestDto);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        accommodationNotificationUtil.notifyAccommodationCreated(savedAccommodation);
        return accommodationMapper.intoDto(savedAccommodation);
    }

    @Override
    public AccommodationResponseDto getAccommodationById(Long id) {
        Accommodation accommodation = retrieveAccommodationById(id);
        return accommodationMapper.intoDto(accommodation);
    }

    @Override
    public Page<AccommodationResponseDto> getAllAccommodations(Pageable pageable) {
        return accommodationRepository.findAll(pageable)
                .map(accommodationMapper::intoDto);
    }

    @Override
    public AccommodationResponseDto updateAccommodation(
            Long id, AccommodationRequestDto requestDto) {

        Accommodation accommodation = retrieveAccommodationById(id);
        accommodationMapper.updateModelFromDto(requestDto, accommodation);
        Accommodation updatedAccommodation = accommodationRepository.save(accommodation);
        return accommodationMapper.intoDto(updatedAccommodation);
    }

    @Override
    public void deleteAccommodation(Long id) {
        accommodationRepository.deleteById(id);
    }

    private Accommodation retrieveAccommodationById(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with ID: " + id));
    }
}
