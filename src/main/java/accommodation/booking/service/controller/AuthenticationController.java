package accommodation.booking.service.controller;

import accommodation.booking.service.dto.user.request.UserLoginRequestDto;
import accommodation.booking.service.dto.user.request.UserRegistrationRequestDto;
import accommodation.booking.service.dto.user.response.UserLoginResponseDto;
import accommodation.booking.service.dto.user.response.UserResponseDto;
import accommodation.booking.service.exceptions.RegistrationException;
import accommodation.booking.service.security.AuthenticationService;
import accommodation.booking.service.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for managing users.")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user.")
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto request)
            throws RegistrationException {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login an authenticated user.")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto request) {
        return authenticationService.authenticate(request);
    }
}
