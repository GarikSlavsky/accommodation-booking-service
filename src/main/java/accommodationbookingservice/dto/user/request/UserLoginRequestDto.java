package accommodationbookingservice.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request DTO for a registered user signing in.")
public class UserLoginRequestDto {
    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Invalid email address format.")
    @Schema(description = "The user's email address. "
            + "This must be a valid email format (e.g., example@email.com).",
            example = "emily@email.com")
    private String email;

    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters.")
    @Schema(description = "The user's password. It must be between 8 and 20 characters.",
            example = "PaSsWorD789")
    private String password;
}
