package accommodationbookingservice.security;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import accommodationbookingservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Cannot find user by email: " + email)
        );
    }
}
