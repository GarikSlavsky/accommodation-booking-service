package mate.academy.accommodationbookingservice.mapper;

import mate.academy.accommodationbookingservice.config.MapperConfig;
import mate.academy.accommodationbookingservice.dto.accommodation.AccommodationRequestDto;
import mate.academy.accommodationbookingservice.dto.accommodation.AccommodationResponseDto;
import mate.academy.accommodationbookingservice.model.Accommodation;
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
