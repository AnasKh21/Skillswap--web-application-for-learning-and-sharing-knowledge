package com.skillswap.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Intercepts every HTTP request and validates the JWT access token.
 *
 * Security points addressed:
 * - Reads token from HttpOnly cookie "access_token" (not localStorage)
 *   → JavaScript cannot steal it via XSS
 * - If token is missing or invalid: does NOT throw — just lets the request
 *   continue unauthenticated. SecurityConfig then blocks protected endpoints
 *   via JwtAuthEntryPoint (clean 401, no stack trace).
 * - If token is valid: loads the user from DB and puts it in the SecurityContext
 *   so controllers can access it via @AuthenticationPrincipal
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractTokenFromCookie(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtUtil.extractEmail(token);

            if (email != null) {
                // Token is valid — load user from DB and set authentication
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credentials (not needed after auth)
                                userDetails.getAuthorities()
                        );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Puts the authenticated user into the SecurityContext.
                // After this line, @AuthenticationPrincipal works in controllers.
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            // If email is null: token invalid/expired → do nothing → request stays unauthenticated
        }

        // Always continue the filter chain — SecurityConfig decides what to block
        filterChain.doFilter(request, response);
    }

    // Reads the "access_token" cookie from the request.
    // Returns null if the cookie is absent — never throws.
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> "access_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
