package com.example.skillswap.config;

import com.example.skillswap.entity.User;
import com.example.skillswap.enums.Role;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilsTest {

    private static final String STRONG_SECRET =
            "0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void weakSecretIsRejected() {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "too-short");

        assertThrows(WeakKeyException.class, jwtUtils::validateSecret);
    }

    @Test
    void strongSecretGeneratesAndValidatesToken() {
        JwtUtils jwtUtils = jwtUtils(STRONG_SECRET);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        String token = jwtUtils.generateToken(user);

        assertTrue(jwtUtils.validateJwtToken(token));
        assertTrue(jwtUtils.isTokenVersionValid(token, user));
        assertEquals("user@example.com", jwtUtils.getUserFromToken(token));

        user.setTokenVersion(1);
        assertFalse(jwtUtils.isTokenVersionValid(token, user));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtUtils jwtUtils = jwtUtils(STRONG_SECRET);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        String token = jwtUtils.generateToken(user);

        assertFalse(jwtUtils.validateJwtToken(token + "tampered"));
    }

    private JwtUtils jwtUtils(String secret) {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", 60_000L);
        jwtUtils.validateSecret();
        return jwtUtils;
    }
}
