package mate.academy.accommodationbookingservice.dto.user.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdateRequestDto {
    @NotNull(message = "Role cannot be null.")
    private String role;
}

