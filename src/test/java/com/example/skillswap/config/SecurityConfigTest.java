package com.example.skillswap.config;

import com.example.skillswap.filter.JwtFilter;
import com.example.skillswap.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void corsAllowsOnlyConfiguredFrontendOrigin() {
        SecurityConfig securityConfig = new SecurityConfig(
                mock(CustomUserDetailsService.class),
                mock(JwtFilter.class)
        );
        ReflectionTestUtils.setField(
                securityConfig,
                "frontendUrl",
                "https://app.skillswap.example"
        );

        CorsConfiguration cors = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest(
                        "GET",
                        "/api/auth/login"
                ));

        assertEquals(
                "https://app.skillswap.example",
                cors.checkOrigin("https://app.skillswap.example")
        );
        assertNull(cors.checkOrigin("http://localhost:5173"));
    }
}
