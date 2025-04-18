package accommodationbookingservice.dto.user.response;

import accommodationbookingservice.model.User;
import java.util.Set;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<User.UserRole> roles;
}
