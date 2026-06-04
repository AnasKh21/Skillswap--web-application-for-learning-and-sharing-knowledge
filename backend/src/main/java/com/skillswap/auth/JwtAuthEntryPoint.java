package com.skillswap.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Called by Spring Security when a request reaches a protected endpoint
 * without a valid token (or with an expired/invalid one).
 *
 * Security point: returns a clean 401 JSON response with NO stack trace.
 * A default Spring Security error page leaks server info — this prevents that.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Return a minimal JSON body — no exception message, no stack trace
        Map<String, String> body = Map.of(
            "error", "Unauthorized",
            "message", "Valid authentication token required"
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
