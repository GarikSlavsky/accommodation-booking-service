package accommodation.booking.service.validation.bookingdate;

import accommodation.booking.service.dto.booking.BookingRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckInBeforeCheckOutValidator
        implements ConstraintValidator<CheckInBeforeCheckOut, BookingRequestDto> {

    @Override
    public boolean isValid(BookingRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null) {
            return true;
        }
        return dto.getCheckInDate().isBefore(dto.getCheckOutDate())
                || dto.getCheckInDate().equals(dto.getCheckOutDate());
    }
}
