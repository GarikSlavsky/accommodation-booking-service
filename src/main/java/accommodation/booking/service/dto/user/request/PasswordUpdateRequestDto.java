package accommodation.booking.service.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateRequestDto {
    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters.")
    @Schema(description = "The user's password. It must be between 8 and 20 characters.",
            example = "SecurePassword123")
    private String newPassword;
}
