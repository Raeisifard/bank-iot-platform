package com.isc.common.web.security;

import com.isc.common.security.jwt.JwtValidatorService;
import com.isc.common.security.model.SecurityPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Extracts a Bearer JWT and delegates all JWT validation and principal
 * construction to common-security.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtValidatorService jwtValidatorService;

    public JwtAuthenticationFilter(JwtValidatorService jwtValidatorService) {
        this.jwtValidatorService = jwtValidatorService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            log.warn("Rejected request with empty bearer token: {}", request.getRequestURI());
            unauthorized(response, "Bearer token is empty");
            return;
        }

        try {
            SecurityPrincipal principal = jwtValidatorService.validateAndExtractPrincipal(token);

            var authorities = principal.getAuthorities().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableSet());

            var authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    token,
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
            log.warn("Rejected invalid bearer token for {}: {}", request.getRequestURI(), ex.getMessage());
            unauthorized(response, "Invalid or expired bearer token");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals("/error");
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        if (response.isCommitted()) return;
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" +
                message + "\"}");
    }
}
