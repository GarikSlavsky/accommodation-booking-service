package accommodationbookingservice.mapper;

import accommodationbookingservice.config.MapperConfig;
import accommodationbookingservice.dto.booking.BookingRequestDto;
import accommodationbookingservice.dto.booking.BookingResponseDto;
import accommodationbookingservice.model.Accommodation;
import accommodationbookingservice.model.Booking;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface BookingMapper {
    @Mapping(target = "accommodationId", source = "accommodation.id")
    @Mapping(target = "userId", source = "user.id")
    BookingResponseDto intoDto(Booking booking);

    @Mapping(target = "accommodation", source = "accommodationId",
            qualifiedByName = "accommodationFromId")
    Booking intoModel(BookingRequestDto bookingRequestDto);

    @Mapping(target = "accommodation", source = "accommodationId",
            qualifiedByName = "accommodationFromId")
    void updateModelFromDto(@MappingTarget Booking booking, BookingRequestDto bookingRequestDto);

    @Named("accommodationFromId")
    default Accommodation getAccommodation(Long id) {
        return Optional.ofNullable(id)
                .map(Accommodation::new)
                .orElse(null);
    }
}
