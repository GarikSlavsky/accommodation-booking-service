package mate.academy.accommodationbookingservice.service.user;

import mate.academy.accommodationbookingservice.dto.user.request.PasswordUpdateRequestDto;
import mate.academy.accommodationbookingservice.dto.user.request.UserRegistrationRequestDto;
import mate.academy.accommodationbookingservice.dto.user.request.UserUpdateRequestDto;
import mate.academy.accommodationbookingservice.dto.user.response.UserResponseDto;
import mate.academy.accommodationbookingservice.exceptions.RegistrationException;

public interface UserService {
    UserResponseDto updateUserName(String email, UserUpdateRequestDto updateDto);

    UserResponseDto updateUserPassword(String email, PasswordUpdateRequestDto passwordRequestDto);

    UserResponseDto getUserProfileByEmail(String email);

    UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserResponseDto updateUserRole(Long id, String newRole);
}
