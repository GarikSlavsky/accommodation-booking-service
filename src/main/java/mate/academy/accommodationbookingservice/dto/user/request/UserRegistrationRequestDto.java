package mate.academy.accommodationbookingservice.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import mate.academy.accommodationbookingservice.validation.password.FieldMatch;

@Data
@FieldMatch
@Schema(description = "Request DTO for registering a new user.")
public class UserRegistrationRequestDto {

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Invalid email address format.")
    @Schema(description = "The user's email address.",
            example = "john.doe@email.com")
    private String email;

    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters.")
    @Schema(description = "The user's password. It must be between 8 and 20 characters.",
            example = "SecurePassword123")
    private String password;

    @NotBlank(message = "Password confirmation cannot be blank.")
    @Size(min = 8, max = 20, message = "Password confirmation must be between 8 and 20 characters.")
    @Schema(description = "The confirmation password. It should match the password field.",
            example = "SecurePassword123")
    private String confirmPassword;

    @NotBlank(message = "First name cannot be blank.")
    @Schema(description = "The first name of the registering user.",
            example = "John")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank.")
    @Schema(description = "The last name of the registering user.",
            example = "Doe")
    private String lastName;
}

