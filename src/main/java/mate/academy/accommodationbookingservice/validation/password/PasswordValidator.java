package mate.academy.accommodationbookingservice.validation.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import mate.academy.accommodationbookingservice.dto.user.request.UserRegistrationRequestDto;

public class PasswordValidator
        implements ConstraintValidator<FieldMatch, UserRegistrationRequestDto> {

    @Override
    public boolean isValid(UserRegistrationRequestDto dto, ConstraintValidatorContext context) {
        return Objects.equals(dto.getPassword(), dto.getConfirmPassword());
    }
}
