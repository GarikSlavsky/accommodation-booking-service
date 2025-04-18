package accommodationbookingservice.dto.user.request;

import accommodationbookingservice.model.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdateRequestDto {
    @NotNull(message = "Role cannot be null.")
    private User.UserRole role;
}

