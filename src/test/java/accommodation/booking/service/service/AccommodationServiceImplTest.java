package accommodation.booking.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import accommodation.booking.service.dto.accommodation.AccommodationRequestDto;
import accommodation.booking.service.dto.accommodation.AccommodationResponseDto;
import accommodation.booking.service.mapper.AccommodationMapper;
import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.repository.AccommodationRepository;
import accommodation.booking.service.service.accommodation.AccommodationServiceImpl;
import accommodation.booking.service.service.notification.AccommodationNotificationUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccommodationServiceImplTest {
    private static final Long ACTUAL_ID = 1L;
    private static final BigDecimal INITIAL_DAILY_RATE = BigDecimal.valueOf(150.00);
    private static final BigDecimal NEW_DAILY_RATE = BigDecimal.valueOf(170.00);
    private AccommodationRequestDto requestDto;
    private Accommodation accommodation;
    private AccommodationResponseDto expected;

    @Mock
    private AccommodationNotificationUtil accommodationNotificationUtil;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationMapper accommodationMapper;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    @BeforeEach
    void setUp() {
        requestDto = initializeAccommodationRequestDto();
        accommodation = initializeAccommodation(requestDto);
        expected = initializeAccommodationResponseDto(accommodation);
    }

    @Test
    @DisplayName("Add accommodation with valid request DTO returns correct response DTO")
    void addAccommodation_ValidRequestDto_ReturnsAccommodationResponseDto() {
        // Given: Valid request DTO and mocked behavior
        when(accommodationMapper.intoModel(requestDto)).thenReturn(accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.intoDto(accommodation)).thenReturn(expected);

        // When: Call the service method
        AccommodationResponseDto actual = accommodationService.addAccommodation(requestDto);

        // Then: Verify the result and interactions
        assertThat(actual).isEqualTo(expected);
        verify(accommodationMapper).intoModel(requestDto);
        verify(accommodationRepository).save(accommodation);
        verify(accommodationNotificationUtil).notifyAccommodationCreated(accommodation);
        verify(accommodationMapper).intoDto(accommodation);
    }

    @Test
    @DisplayName("Add accommodation with invalid request DTO throws exception")
    void addAccommodation_InvalidRequestDto_ThrowsException() {
        // Given: Invalid request DTO with null type
        AccommodationRequestDto invalidRequestDto = initializeAccommodationRequestDto();
        invalidRequestDto.setType(null);

        when(accommodationMapper.intoModel(invalidRequestDto))
                .thenThrow(IllegalArgumentException.class);

        // When/Then: Verify that an exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> accommodationService.addAccommodation(invalidRequestDto));
        verify(accommodationRepository, never()).save(any(Accommodation.class));
        verify(accommodationNotificationUtil, never())
                .notifyAccommodationCreated(any(Accommodation.class));
    }

    @Test
    @DisplayName("Update accommodation with valid ID and request DTO returns updated response DTO")
    void updateAccommodation_ValidIdAndRequestDto_ReturnsUpdatedAccommodationResponseDto() {
        // Given: Modified request DTO with updated daily rate and amenities
        requestDto.setDailyRate(NEW_DAILY_RATE);
        requestDto.getAmenities().add("Amenity");
        accommodation = initializeAccommodation(requestDto);
        expected = initializeAccommodationResponseDto(accommodation);

        when(accommodationRepository.findById(ACTUAL_ID)).thenReturn(Optional.of(accommodation));
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.intoDto(accommodation)).thenReturn(expected);

        // When: Call the service method
        AccommodationResponseDto actual =
                accommodationService.updateAccommodation(ACTUAL_ID, requestDto);

        // Then: Verify the result and interactions
        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getDailyRate()).isEqualTo(NEW_DAILY_RATE);
        assertThat(actual.getAmenities()).contains("Amenity");
        verify(accommodationRepository).findById(ACTUAL_ID);
        verify(accommodationMapper).updateModelFromDto(requestDto, accommodation);
        verify(accommodationRepository).save(accommodation);
        verify(accommodationMapper).intoDto(accommodation);
    }

    private AccommodationRequestDto initializeAccommodationRequestDto() {
        AccommodationRequestDto dto = new AccommodationRequestDto();
        dto.setType(Accommodation.AccommodationType.CONDO);
        dto.setLocation("New York");
        dto.setSize("Standard");
        dto.setAmenities(new ArrayList<>(Arrays.asList("WiFi", "Pool")));
        dto.setDailyRate(INITIAL_DAILY_RATE);
        dto.setAvailability(10);
        return dto;
    }

    private Accommodation initializeAccommodation(AccommodationRequestDto dto) {
        Accommodation accommodation = new Accommodation();
        accommodation.setType(dto.getType());
        accommodation.setLocation(dto.getLocation());
        accommodation.setSize(dto.getSize());
        accommodation.setAmenities(dto.getAmenities());
        accommodation.setDailyRate(dto.getDailyRate());
        accommodation.setAvailability(dto.getAvailability());
        return accommodation;
    }

    private AccommodationResponseDto initializeAccommodationResponseDto(
            Accommodation accommodation) {

        AccommodationResponseDto dto = new AccommodationResponseDto();
        dto.setId(ACTUAL_ID);
        dto.setType(accommodation.getType());
        dto.setLocation(accommodation.getLocation());
        dto.setSize(accommodation.getSize());
        dto.setAmenities(accommodation.getAmenities());
        dto.setDailyRate(accommodation.getDailyRate());
        dto.setAvailability(accommodation.getAvailability());
        return dto;
    }
}
