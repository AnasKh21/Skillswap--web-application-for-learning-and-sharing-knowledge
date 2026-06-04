package com.skillswap.auth;

import com.skillswap.user.User;
import com.skillswap.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Tells Spring Security how to load a user from the database.
 *
 * Spring Security calls this during JWT validation:
 *   JwtAuthFilter extracts the email from the token
 *   → calls loadUserByUsername(email)
 *   → returns a UserDetails that Spring puts in the SecurityContext
 *   → controllers access it via @AuthenticationPrincipal
 *
 * "Username" in Spring Security = "email" in our app.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));

        // Spring Security's built-in User (not our User entity) — wraps the data
        // that Spring needs: username, password, and roles.
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())         // BCrypt hash stored in DB
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}
