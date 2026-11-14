package com.example.skillswap.service;

import com.example.skillswap.config.JwtUtils;
import com.example.skillswap.dto.request.LoginRequestDTO;
import com.example.skillswap.dto.request.RegisterRequestDTO;
import com.example.skillswap.dto.response.AuthResponse;
import com.example.skillswap.dto.response.RegistrationResponse;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AccountStatus;
import com.example.skillswap.enums.Role;
import com.example.skillswap.exception.AccountBlockedException;
import com.example.skillswap.exception.EmailDeliveryException;
import com.example.skillswap.exception.EmailNotVerifiedException;
import com.example.skillswap.exception.RateLimitExceededException;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AccountStatusService accountStatusService;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private RateLimitService rateLimitService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(
                userRepo,
                passwordEncoder,
                authenticationManager,
                jwtUtils,
                accountStatusService,
                emailVerificationService,
                rateLimitService
        );
    }

    private RegisterRequestDTO registerRequest(String email, String phone) {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setFirstName("New");
        dto.setLastName("User");
        dto.setEmail("  " + email + "  ");
        dto.setPhone(phone);
        dto.setPassword("password123");
        dto.setCity("Paris");
        return dto;
    }

    @Test
    void registrationAlwaysCreatesRegularUser() {
        RegisterRequestDTO dto = registerRequest("USER@Example.com", "+33 6 12 34 56 78");

        when(userRepo.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepo.existsByPhone("+33612345678")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(5L);
            return user;
        });
        RegistrationResponse response = service.register(dto);

        assertTrue(response.getMessage().contains("check your email"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(Role.USER, saved.getRole());
        assertEquals("+33612345678", saved.getPhone());
        assertEquals(AccountStatus.ACTIVE, saved.getAccountStatus());
        assertFalse(saved.isEmailVerified());
        assertFalse(saved.isPhoneVerified());
        verify(emailVerificationService).issueVerificationToken(saved);
        verify(jwtUtils, never()).generateToken(any());
    }

    @Test
    void registrationDoesNotRevealExistingEmail() {
        when(userRepo.existsByEmail("user@example.com")).thenReturn(true);

        RegistrationResponse response =
                service.register(registerRequest("user@example.com", null));

        assertTrue(response.getMessage().contains("If the submitted details are valid"));
        verify(userRepo, never()).save(any());
    }

    @Test
    void registrationDoesNotRevealReusedPhone() {
        when(userRepo.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepo.existsByPhone("+33612345678")).thenReturn(true);

        RegistrationResponse response = service.register(
                registerRequest("user@example.com", "+33 6 12 34 56 78")
        );

        assertTrue(response.getMessage().contains("If the submitted details are valid"));
        verify(userRepo, never()).save(any());
    }

    @Test
    void registrationDoesNotReportSuccessWhenVerificationEmailFails() {
        RegisterRequestDTO dto = registerRequest("user@example.com", null);
        when(userRepo.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new EmailDeliveryException(
                "The verification email could not be sent. Please try again later.",
                new RuntimeException("SMTP failure")
        )).when(emailVerificationService).issueVerificationToken(any(User.class));

        assertThrows(EmailDeliveryException.class, () -> service.register(dto));
        verify(jwtUtils, never()).generateToken(any());
    }

    @Test
    void bannedUserCannotLogIn() {
        LoginRequestDTO dto = new LoginRequestDTO("banned@example.com", "password123");
        User banned = new User();
        banned.setId(2L);
        banned.setEmail("banned@example.com");
        banned.setAccountStatus(AccountStatus.BANNED);

        when(userRepo.findByEmail("banned@example.com")).thenReturn(Optional.of(banned));
        doThrow(new AccountBlockedException("This account has been permanently banned."))
                .when(accountStatusService).assertActive(banned);

        assertThrows(AccountBlockedException.class, () -> service.login(dto));
        verify(jwtUtils, never()).generateToken(any());
    }

    @Test
    void unverifiedUserCannotLogIn() {
        LoginRequestDTO dto = new LoginRequestDTO("unverified@example.com", "password123");
        User unverified = new User();
        unverified.setId(2L);
        unverified.setEmail("unverified@example.com");
        unverified.setRole(Role.USER);
        unverified.setAccountStatus(AccountStatus.ACTIVE);
        unverified.setEmailVerified(false);

        when(userRepo.findByEmail("unverified@example.com")).thenReturn(Optional.of(unverified));

        assertThrows(EmailNotVerifiedException.class, () -> service.login(dto));
        verify(authenticationManager).authenticate(any());
        verify(jwtUtils, never()).generateToken(any());
    }

    @Test
    void invalidCredentialsCannotGenerateToken() {
        LoginRequestDTO dto = new LoginRequestDTO("user@example.com", "wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> service.login(dto)
        );
        verify(userRepo, never()).findByEmail(anyString());
        verify(jwtUtils, never()).generateToken(any());
    }

    @Test
    void activeUserCanLogIn() {
        LoginRequestDTO dto = new LoginRequestDTO("  active@example.com ", "password123");
        User active = new User();
        active.setId(2L);
        active.setFirstName("Active");
        active.setEmail("active@example.com");
        active.setRole(Role.USER);
        active.setAccountStatus(AccountStatus.ACTIVE);
        active.setEmailVerified(true);

        when(userRepo.findByEmail("active@example.com")).thenReturn(Optional.of(active));
        when(jwtUtils.generateToken(active)).thenReturn("token");

        AuthResponse response = service.login(dto);

        assertEquals("token", response.getToken());
        verify(accountStatusService).assertActive(active);
        verify(authenticationManager).authenticate(any());
        verify(rateLimitService).reset("login:active@example.com");
    }

    @Test
    void activeAdminCanLogIn() {
        LoginRequestDTO dto = new LoginRequestDTO("admin@example.com", "password123");
        User admin = new User();
        admin.setId(3L);
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);
        admin.setEmailVerified(true);

        when(userRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(jwtUtils.generateToken(admin)).thenReturn("admin-token");

        AuthResponse response = service.login(dto);

        assertEquals("admin-token", response.getToken());
        assertEquals(Role.ADMIN, response.getRole());
        verify(jwtUtils).generateToken(admin);
    }

    @Test
    void loginRejectsWhenRateLimitIsExceeded() {
        LoginRequestDTO dto = new LoginRequestDTO("active@example.com", "password123");

        doThrow(new RateLimitExceededException("Too many attempts."))
                .when(rateLimitService)
                .consume(eq("login:active@example.com"), anyInt(), any());

        assertThrows(RateLimitExceededException.class, () -> service.login(dto));
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void logoutAllInvalidatesExistingTokens() {
        User active = new User();
        active.setEmail("active@example.com");
        active.setTokenVersion(4);

        when(userRepo.findByEmail("active@example.com")).thenReturn(Optional.of(active));

        service.logoutAll("  ACTIVE@example.com ");

        assertEquals(5, active.getTokenVersion());
        verify(userRepo).save(active);
    }

    @Test
    void verifyEmailDelegatesToVerificationService() {
        RegistrationResponse response = service.verifyEmail("verification-token");

        assertTrue(response.getMessage().contains("verified"));
        verify(emailVerificationService).verifyEmail("verification-token");
    }

    @Test
    void resendVerificationReturnsGenericMessage() {
        RegistrationResponse response =
                service.resendVerificationEmail("user@example.com");

        assertTrue(response.getMessage().contains("If an unverified account exists"));
        verify(emailVerificationService)
                .resendVerificationEmail("user@example.com");
    }

    @Test
    void resendVerificationIsRateLimitedBeforeSending() {
        doThrow(new RateLimitExceededException("Too many attempts."))
                .when(rateLimitService)
                .consume(eq("resend:user@example.com"), anyInt(), any());

        assertThrows(
                RateLimitExceededException.class,
                () -> service.resendVerificationEmail("USER@example.com")
        );
        verifyNoInteractions(emailVerificationService);
    }
}
