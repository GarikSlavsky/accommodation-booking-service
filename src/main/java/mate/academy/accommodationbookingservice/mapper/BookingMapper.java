package mate.academy.accommodationbookingservice.mapper;

import java.util.Optional;
import mate.academy.accommodationbookingservice.config.MapperConfig;
import mate.academy.accommodationbookingservice.dto.booking.BookingRequestDto;
import mate.academy.accommodationbookingservice.dto.booking.BookingResponseDto;
import mate.academy.accommodationbookingservice.model.Accommodation;
import mate.academy.accommodationbookingservice.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface BookingMapper {
    @Mapping(target = "accommodationId", source = "accommodation.id")
    @Mapping(target = "userId", source = "user.id")
    BookingResponseDto intoDto(Booking booking);

    @Mapping(target = "accommodation", source = "accommodationId",
            qualifiedByName = "accommodationFromId")
    Booking intoModel(BookingRequestDto bookingRequestDto);

    @Named("accommodationFromId")
    default Accommodation getAccommodation(Long id) {
        return Optional.ofNullable(id)
                .map(Accommodation::new)
                .orElse(null);
    }
}
