package accommodation.booking.service.dto.user.request;

import accommodation.booking.service.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdateRequestDto {
    @NotNull(message = "Role cannot be null.")
    private Role.RoleName role;
}

