package com.isc.common.web.security;

import com.isc.common.security.jwt.JwtValidatorService;
import com.isc.common.security.model.SecurityPrincipal;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtValidatorService validator = mock(JwtValidatorService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);

    @AfterEach
    void cleanup() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateAuthenticationFromValidBearerToken() throws Exception {
        SecurityPrincipal principal = new SecurityPrincipal("user1", Set.of("ROLE_USER"), Map.of(), null);
        when(validator.validateAndExtractPrincipal("abc")).thenReturn(principal);

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(principal, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldReturn401ForInvalidToken() throws Exception {
        when(validator.validateAndExtractPrincipal("bad"))
                .thenThrow(new RuntimeException("invalid"));

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication());
        verifyNoInteractions(chain);
    }
}
