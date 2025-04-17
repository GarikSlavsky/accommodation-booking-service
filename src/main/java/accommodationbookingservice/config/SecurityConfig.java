package accommodationbookingservice.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import lombok.RequiredArgsConstructor;
import accommodationbookingservice.security.CustomUserDetailService;
import accommodationbookingservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                antMatcher("/auth/**"),
                                antMatcher("/swagger-ui/**"),
                                antMatcher("/v3/api-docs/**"),
                                antMatcher(HttpMethod.GET, "/accommodations/**"))
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .httpBasic(withDefaults())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(userDetailsService)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
