package accommodationbookingservice.validation.bookingdate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import accommodationbookingservice.dto.booking.BookingRequestDto;

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
