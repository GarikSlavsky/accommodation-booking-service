package accommodationbookingservice.dto.user.response;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}
