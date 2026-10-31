package com.example.skillswap.filter;

import com.example.skillswap.config.JwtUtils;
import com.example.skillswap.entity.User;
import com.example.skillswap.service.AccountStatusService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {
    @Mock
    private JwtUtils jwtUtil;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private AccountStatusService accountStatusService;
    @Mock
    private FilterChain filterChain;

    private JwtFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtFilter(jwtUtil, userDetailsService, accountStatusService);
    }

    @Test
    void bannedUserWithValidOldTokenIsRejected() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer old-valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User banned = new User();
        banned.setId(2L);
        banned.setEmail("banned@example.com");

        when(jwtUtil.validateJwtToken("old-valid-token")).thenReturn(true);
        when(jwtUtil.getUserFromToken("old-valid-token")).thenReturn("banned@example.com");
        when(userDetailsService.loadUserByUsername("banned@example.com")).thenReturn(banned);
        when(accountStatusService.isBlocked(banned)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("suspended or banned"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void activeUserTokenContinuesFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User active = new User();
        active.setId(3L);
        active.setEmail("active@example.com");
        active.setRole(com.example.skillswap.enums.Role.USER);

        when(jwtUtil.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUserFromToken("valid-token")).thenReturn("active@example.com");
        when(userDetailsService.loadUserByUsername("active@example.com")).thenReturn(active);
        when(accountStatusService.isBlocked(active)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }
}
