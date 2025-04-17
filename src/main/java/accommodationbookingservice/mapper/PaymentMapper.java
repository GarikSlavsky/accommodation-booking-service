package accommodationbookingservice.mapper;

import accommodationbookingservice.config.MapperConfig;
import accommodationbookingservice.dto.payment.PaymentResponseDto;
import accommodationbookingservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(target = "bookingId", source = "booking.id")
    PaymentResponseDto intoDto(Payment payment);
}
