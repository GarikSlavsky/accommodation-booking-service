package accommodation.booking.service.util;

import accommodation.booking.service.model.Role;
import accommodation.booking.service.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationTestUtil {
    public static void authenticateUser(Long userId, String email, Role.RoleName name) {
        User testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(email);
        Role role = new Role();
        role.setName(name);
        testUser.getRoles().add(role);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                testUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
