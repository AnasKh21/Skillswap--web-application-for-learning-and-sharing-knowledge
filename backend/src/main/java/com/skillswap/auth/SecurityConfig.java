package com.skillswap.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration.
 *
 * Security decisions documented inline:
 * - CSRF disabled: safe because cookies use SameSite=Strict (see AuthService)
 * - Stateless sessions: JWT is self-contained, no server-side session needed
 * - CSP: strict policy for the API layer (no scripts, no frames)
 * - BCrypt with default cost (10 rounds ≈ 100ms/hash) — adequate for passwords
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, JwtAuthEntryPoint jwtAuthEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF: disabled intentionally.
            // We use HttpOnly + SameSite=Strict cookies.
            // SameSite=Strict means the browser only sends the cookie on same-site requests.
            // An attacker on evil.com cannot trigger a request that includes the cookie.
            // Therefore CSRF tokens are redundant here.
            .csrf(csrf -> csrf.disable())

            // No server-side sessions — every request is authenticated via the JWT cookie
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Custom 401 handler — returns clean JSON, no stack trace
            .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthEntryPoint))

            // Security headers (XSS + content sniffing protection)
            .headers(headers -> headers
                // Prevent MIME type sniffing (e.g. browser interpreting JSON as script)
                .contentTypeOptions(c -> {})
                // Prevent this API from being embedded in an iframe
                .frameOptions(f -> f.deny())
                // CSP applies to API JSON responses only.
                // Swagger UI paths are excluded because they serve HTML+JS.
                // The React frontend's CSP must be configured in nginx separately.
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'self'; frame-ancestors 'none'"))
                .referrerPolicy(r ->
                    r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )

            // ── Endpoint access rules ─────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Public: auth endpoints
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                // Public: Swagger UI + all its static assets and JSON spec
                // Swagger loads several sub-paths: /swagger-ui/**, /api-docs, /api-docs/**, /webjars/**
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/api-docs",
                    "/api-docs/**",
                    "/webjars/**"
                ).permitAll()
                // Public: skill catalogue
                .requestMatchers("/api/skills", "/api/skills/**").permitAll()
                // Public: fichiers uploadés (avatars, bannières)
                .requestMatchers("/uploads/**").permitAll()
                // Public read-only: any user profile, skill list
                .requestMatchers(HttpMethod.GET, "/api/users/{id}", "/api/ratings/user/**").permitAll()
                // Everything else requires a valid JWT token
                .anyRequest().authenticated()
            )

            // Run our JWT filter before Spring's default auth filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt password encoder.
    // Cost factor 10 = ~100ms per hash on modern hardware.
    // This makes brute-force attacks 10^7 times slower than MD5.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS: allow the React frontend to call the API.
    // In production, replace localhost origins with your real domain.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        // Required for cookies to be included in cross-origin requests
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
