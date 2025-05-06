package accommodation.booking.service.mapper;

import accommodation.booking.service.config.MapperConfig;
import accommodation.booking.service.dto.payment.PaymentResponseDto;
import accommodation.booking.service.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(target = "bookingId", source = "booking.id")
    PaymentResponseDto intoDto(Payment payment);
}
