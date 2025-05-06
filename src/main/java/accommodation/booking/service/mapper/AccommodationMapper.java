package accommodation.booking.service.mapper;

import accommodation.booking.service.config.MapperConfig;
import accommodation.booking.service.dto.accommodation.AccommodationRequestDto;
import accommodation.booking.service.dto.accommodation.AccommodationResponseDto;
import accommodation.booking.service.model.Accommodation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface AccommodationMapper {
    AccommodationResponseDto intoDto(Accommodation accommodation);

    Accommodation intoModel(AccommodationRequestDto accommodationRequestDto);

    void updateModelFromDto(
            AccommodationRequestDto accommodationRequestDto,
            @MappingTarget Accommodation accommodation
    );
}
