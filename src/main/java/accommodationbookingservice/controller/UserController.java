package accommodationbookingservice.controller;

import accommodationbookingservice.dto.user.request.PasswordUpdateRequestDto;
import accommodationbookingservice.dto.user.request.UserRoleUpdateRequestDto;
import accommodationbookingservice.dto.user.request.UserUpdateRequestDto;
import accommodationbookingservice.dto.user.response.UserResponseDto;
import accommodationbookingservice.model.User;
import accommodationbookingservice.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for managing users.")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @PatchMapping("/me/profile")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Allows users to update their profile information.")
    public UserResponseDto updateUserProfile(
            @RequestBody @Valid UserUpdateRequestDto updateRequestDto,
            Authentication authentication
    ) {

        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        return userService.updateUserName(email, updateRequestDto);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Allows users to update their password.")
    public UserResponseDto updateUserPassword(
            @RequestBody PasswordUpdateRequestDto passwordUpdateRequestDto,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        return userService.updateUserPassword(email, passwordUpdateRequestDto);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieves the profile information for the currently logged-in user.")
    public UserResponseDto getCurrentUserProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        return userService.getUserProfileByEmail(email);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/role")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Updates the role of a specific user.")
    public UserResponseDto updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleUpdateRequestDto updateRequestDto) {

        return userService.updateUserRole(id, updateRequestDto.getRole());
    }
}
