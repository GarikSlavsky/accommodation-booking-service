package accommodationbookingservice.service.user;

import accommodationbookingservice.dto.user.request.PasswordUpdateRequestDto;
import accommodationbookingservice.dto.user.request.UserRegistrationRequestDto;
import accommodationbookingservice.dto.user.request.UserUpdateRequestDto;
import accommodationbookingservice.dto.user.response.UserResponseDto;
import accommodationbookingservice.exceptions.RegistrationException;
import accommodationbookingservice.model.User;

public interface UserService {
    UserResponseDto updateUserName(String email, UserUpdateRequestDto updateDto);

    UserResponseDto updateUserPassword(String email, PasswordUpdateRequestDto passwordRequestDto);

    UserResponseDto getUserProfileByEmail(String email);

    UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserResponseDto updateUserRole(Long id, User.UserRole newRole);
}
