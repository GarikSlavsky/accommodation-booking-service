package accommodation.booking.service.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponseDto {
    private String token;
}
