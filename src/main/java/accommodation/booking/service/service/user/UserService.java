package accommodation.booking.service.service.user;

import accommodation.booking.service.dto.user.request.PasswordUpdateRequestDto;
import accommodation.booking.service.dto.user.request.UserRegistrationRequestDto;
import accommodation.booking.service.dto.user.request.UserUpdateRequestDto;
import accommodation.booking.service.dto.user.response.UserResponseDto;
import accommodation.booking.service.exceptions.RegistrationException;
import accommodation.booking.service.model.Role;

public interface UserService {
    UserResponseDto updateUserName(String email, UserUpdateRequestDto updateDto);

    UserResponseDto updateUserPassword(String email, PasswordUpdateRequestDto passwordRequestDto);

    UserResponseDto getUserProfileByEmail(String email);

    UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserResponseDto updateUserRole(Long id, Role.RoleName newRole);
}
