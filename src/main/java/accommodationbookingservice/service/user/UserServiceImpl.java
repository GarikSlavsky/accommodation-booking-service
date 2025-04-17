package accommodationbookingservice.service.user;

import accommodationbookingservice.dto.user.request.PasswordUpdateRequestDto;
import accommodationbookingservice.dto.user.request.UserRegistrationRequestDto;
import accommodationbookingservice.dto.user.request.UserUpdateRequestDto;
import accommodationbookingservice.dto.user.response.UserResponseDto;
import accommodationbookingservice.exceptions.RegistrationException;
import accommodationbookingservice.mapper.UserMapper;
import accommodationbookingservice.model.User;
import accommodationbookingservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto updateUserName(String email, UserUpdateRequestDto updateDto) {
        User user = retrieveUserByEmail(email);
        userMapper.updateModelFromDto(updateDto, user);
        User updatedUser = userRepository.save(user);
        return userMapper.intoDto(updatedUser);
    }

    @Override
    public UserResponseDto updateUserPassword(
            String email, PasswordUpdateRequestDto passwordRequestDto) {

        User user = retrieveUserByEmail(email);
        user.setPassword(passwordRequestDto.getNewPassword());
        User updatedUser = userRepository.save(user);
        return userMapper.intoDto(updatedUser);
    }

    @Override
    public UserResponseDto getUserProfileByEmail(String email) {
        User user = retrieveUserByEmail(email);
        return userMapper.intoDto(user);

    }

    @Transactional
    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException(
                    "The user with email: " + requestDto.getEmail() + " already exists.");
        }

        User user = userMapper.intoModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.UserRole.ROLE_CUSTOMER);
        userRepository.save(user);
        return userMapper.intoDto(user);
    }

    @Override
    public UserResponseDto updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User by ID : " + id + " not found."));
        try {
            User.UserRole userRole = User.UserRole.valueOf(newRole.toUpperCase());
            user.setRole(userRole);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + newRole);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.intoDto(updatedUser);
    }

    private User retrieveUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User by email : " + email + " not found."));
    }
}
