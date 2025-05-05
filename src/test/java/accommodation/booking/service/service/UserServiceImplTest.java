package accommodation.booking.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import accommodation.booking.service.dto.user.request.UserRegistrationRequestDto;
import accommodation.booking.service.dto.user.response.UserResponseDto;
import accommodation.booking.service.exceptions.RegistrationException;
import accommodation.booking.service.mapper.UserMapper;
import accommodation.booking.service.model.Role;
import accommodation.booking.service.model.User;
import accommodation.booking.service.repository.RoleRepository;
import accommodation.booking.service.repository.UserRepository;
import accommodation.booking.service.service.user.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private static final Long USER_ID = 1L;
    private static final Role.RoleName CUSTOMER = Role.RoleName.ROLE_CUSTOMER;
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final String EMAIL = "john.doe@email.com";
    private static final Role.RoleName NEW_ROLE = Role.RoleName.ROLE_MANAGER;
    private UserRegistrationRequestDto requestDto;
    private UserResponseDto responseDto;
    private User user;
    private Role managerRole;
    private Role customerRole;
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        requestDto = initializeUserRegistrationRequestDto();
        user = initializeUser(requestDto);
        responseDto = initializeUserResponseDto(user);
        customerRole = initializeRole();
        managerRole = initializeRole();
    }

    @Test
    @DisplayName("Register user with unique email returns response DTO")
    void register_UniqueEmail_ReturnsUserResponseDto() throws RegistrationException {
        // Given: Unique email and valid request
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userMapper.intoModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName(CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.intoDto(user)).thenReturn(responseDto);

        // When: Call the service method
        UserResponseDto actual = userService.register(requestDto);

        // Then: Verify the result and interactions
        assertThat(actual).isEqualTo(responseDto);
        verify(userRepository).existsByEmail(requestDto.getEmail());
        verify(userMapper).intoModel(requestDto);
        verify(passwordEncoder).encode(requestDto.getPassword());
        verify(roleRepository).findByName(CUSTOMER);
        verify(userRepository).save(user);
        verify(userMapper).intoDto(user);
    }

    @Test
    @DisplayName("Register user with existing email throws RegistrationException")
    void register_ExistingEmail_ThrowsRegistrationException() {
        // Given: Email already exists
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        // When/Then: Verify that an exception is thrown
        assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));

        // Verify: No further interactions occur after the exception
        verify(userRepository).existsByEmail(requestDto.getEmail());
        verify(userMapper, never()).intoModel(requestDto);
        verify(passwordEncoder, never()).encode(requestDto.getPassword());
        verify(roleRepository, never()).findByName(CUSTOMER);
        verify(userRepository, never()).save(user);
        verify(userMapper, never()).intoDto(user);
    }

    @Test
    @DisplayName("Update user role adds new role and returns response DTO")
    void updateUserRole_ValidUserAndRole_ReturnsUserResponseDto() {
        // Given: Existing user and role
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(NEW_ROLE)).thenReturn(Optional.of(managerRole));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.intoDto(user)).thenReturn(responseDto);

        // When: Call the service method
        UserResponseDto actual = userService.updateUserRole(USER_ID, NEW_ROLE);

        // Then: Verify the result and interactions
        assertThat(actual).isEqualTo(responseDto);
        assertThat(user.getRoles()).contains(managerRole);
        verify(userRepository).findById(USER_ID);
        verify(roleRepository).findByName(NEW_ROLE);
        verify(userRepository).save(user);
        verify(userMapper).intoDto(user);
    }

    @Test
    @DisplayName("Get user profile by email returns response DTO")
    void getUserProfileByEmail_ValidEmail_ReturnsUserResponseDto() {
        // Given: User exists for the given email
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.intoDto(user)).thenReturn(responseDto);

        // When: Call the service method
        UserResponseDto actual = userService.getUserProfileByEmail(EMAIL);

        // Then: Verify the result and interactions
        assertThat(actual).isEqualTo(responseDto);
        verify(userRepository).findByEmail(EMAIL);
        verify(userMapper).intoDto(user);
    }

    private UserRegistrationRequestDto initializeUserRegistrationRequestDto() {
        UserRegistrationRequestDto dto = new UserRegistrationRequestDto();
        dto.setEmail("john.doe@email.com");
        dto.setPassword("SecurePassword123");
        dto.setConfirmPassword("SecurePassword123");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        return dto;
    }

    private User initializeUser(UserRegistrationRequestDto dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        return user;
    }

    private UserResponseDto initializeUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }

    private Role initializeRole() {
        Role role = new Role();
        role.setName(CUSTOMER);
        return role;
    }
}
