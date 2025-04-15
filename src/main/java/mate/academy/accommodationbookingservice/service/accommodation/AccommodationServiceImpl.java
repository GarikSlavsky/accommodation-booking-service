package mate.academy.accommodationbookingservice.service.accommodation;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.accommodationbookingservice.dto.accommodation.AccommodationRequestDto;
import mate.academy.accommodationbookingservice.dto.accommodation.AccommodationResponseDto;
import mate.academy.accommodationbookingservice.mapper.AccommodationMapper;
import mate.academy.accommodationbookingservice.model.Accommodation;
import mate.academy.accommodationbookingservice.notification.NotificationService;
import mate.academy.accommodationbookingservice.repository.AccommodationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final NotificationService notificationService;

    @Override
    public AccommodationResponseDto addAccommodation(AccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationMapper.intoModel(requestDto);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        notificationService.sendNotification(
                String.format("New accommodation created: ID=%d, Type=%s, Location=%s",
                        savedAccommodation.getId(),
                        savedAccommodation.getType(),
                        savedAccommodation.getLocation())
        );

        return accommodationMapper.intoDto(savedAccommodation);
    }

    @Override
    public AccommodationResponseDto getAccommodationById(Long id) {
        Accommodation accommodation = retrieveAccommodationById(id);
        return accommodationMapper.intoDto(accommodation);
    }

    @Override
    public List<AccommodationResponseDto> getAllAccommodations(Pageable pageable) {
        return accommodationRepository.findAll(pageable)
                .stream()
                .filter(accommodation -> !accommodation.isDeleted())
                .map(accommodationMapper::intoDto)
                .toList();
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
