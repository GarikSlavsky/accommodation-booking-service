package accommodation.booking.service.service.user;

import static accommodation.booking.service.model.Role.RoleName.ROLE_CUSTOMER;

import accommodation.booking.service.dto.user.request.PasswordUpdateRequestDto;
import accommodation.booking.service.dto.user.request.UserRegistrationRequestDto;
import accommodation.booking.service.dto.user.request.UserUpdateRequestDto;
import accommodation.booking.service.dto.user.response.UserResponseDto;
import accommodation.booking.service.exceptions.RegistrationException;
import accommodation.booking.service.mapper.UserMapper;
import accommodation.booking.service.model.Role;
import accommodation.booking.service.model.User;
import accommodation.booking.service.repository.RoleRepository;
import accommodation.booking.service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResponseDto updateUserName(String email, UserUpdateRequestDto updateDto) {
        User user = retrieveUserByEmail(email);
        userMapper.updateModelFromDto(updateDto, user);
        User updatedUser = userRepository.save(user);
        return userMapper.intoDto(updatedUser);
    }

    @Transactional
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
        Role role = retrieveRoleByRoleName(ROLE_CUSTOMER);
        user.setRoles(Set.of(role));
        userRepository.save(user);
        return userMapper.intoDto(user);
    }

    @Transactional
    @Override
    public UserResponseDto updateUserRole(Long id, Role.RoleName newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User by ID : " + id + " not found."));

        Role role = retrieveRoleByRoleName(newRole);
        user.getRoles().add(role);
        User updatedUser = userRepository.save(user);
        return userMapper.intoDto(updatedUser);
    }

    private User retrieveUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User by email : " + email + " not found."));
    }

    private Role retrieveRoleByRoleName(Role.RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Error: Role " + roleName + " not found."));
    }
}
