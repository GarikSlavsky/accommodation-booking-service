package accommodation.booking.service.validation.bookingdate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CheckInBeforeCheckOutValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckInBeforeCheckOut {
    String message() default "Check-in date must be before check-out date.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
