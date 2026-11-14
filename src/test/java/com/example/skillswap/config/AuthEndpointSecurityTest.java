package com.example.skillswap.config;

import com.example.skillswap.dto.response.AuthResponse;
import com.example.skillswap.dto.response.RegistrationResponse;
import com.example.skillswap.enums.Role;
import com.example.skillswap.service.AccountStatusService;
import com.example.skillswap.service.AuthService;
import com.example.skillswap.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(com.example.skillswap.controller.AuthController.class)
@Import(SecurityConfig.class)
class AuthEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private AccountStatusService accountStatusService;

    @Test
    void anonymousUserCanRegister() throws Exception {
        when(authService.register(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new RegistrationResponse("Check your email."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Test",
                                  "lastName": "User",
                                  "email": "user@example.com",
                                  "password": "password123",
                                  "city": "Paris"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserCanLogin() throws Exception {
        when(authService.login(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AuthResponse(
                        "test-token",
                        1L,
                        "Test",
                        "User",
                        "user@example.com",
                        "Paris",
                        null,
                        Role.USER
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserCanVerifyEmail() throws Exception {
        when(authService.verifyEmail("verification-token"))
                .thenReturn(new RegistrationResponse("Email verified."));

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"verification-token\"}"))
                .andExpect(status().isOk());

        verify(authService).verifyEmail("verification-token");
    }

    @Test
    void blankVerificationTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousUserCanResendVerificationEmail() throws Exception {
        when(authService.resendVerificationEmail("user@example.com"))
                .thenReturn(new RegistrationResponse("Check your email."));

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk());

        verify(authService).resendVerificationEmail("user@example.com");
    }

    @Test
    void anonymousUserCannotLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());
    }
}
