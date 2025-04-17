package accommodationbookingservice.mapper;

import accommodationbookingservice.config.MapperConfig;
import accommodationbookingservice.dto.accommodation.AccommodationRequestDto;
import accommodationbookingservice.dto.accommodation.AccommodationResponseDto;
import accommodationbookingservice.model.Accommodation;
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
