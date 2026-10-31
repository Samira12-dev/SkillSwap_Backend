package com.example.skillswap.service;

import com.example.skillswap.config.JwtUtils;
import com.example.skillswap.dto.request.LoginRequestDTO;
import com.example.skillswap.dto.request.RegisterRequestDTO;
import com.example.skillswap.dto.response.AuthResponse;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AccountStatus;
import com.example.skillswap.enums.Role;
import com.example.skillswap.exception.AccountBlockedException;
import com.example.skillswap.exception.ConflictException;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(
                userRepo,
                passwordEncoder,
                authenticationManager,
                jwtUtils,
                accountStatusService
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
        when(jwtUtils.generateToken(any(User.class))).thenReturn("token");

        AuthResponse response = service.register(dto);

        assertEquals(Role.USER, response.getRole());
        assertEquals("user@example.com", response.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(Role.USER, saved.getRole());
        assertEquals("+33612345678", saved.getPhone());
        assertEquals(AccountStatus.ACTIVE, saved.getAccountStatus());
        assertFalse(saved.isEmailVerified());
        assertFalse(saved.isPhoneVerified());
    }

    @Test
    void registrationRejectsExistingEmail() {
        when(userRepo.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.register(registerRequest("user@example.com", null))
        );
        verify(userRepo, never()).save(any());
    }

    @Test
    void registrationRejectsReusedPhoneEvenWhenEmailIsNew() {
        when(userRepo.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepo.existsByPhone("+33612345678")).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.register(registerRequest("user@example.com", "+33 6 12 34 56 78"))
        );
        verify(userRepo, never()).save(any());
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
    void activeUserCanLogIn() {
        LoginRequestDTO dto = new LoginRequestDTO("  active@example.com ", "password123");
        User active = new User();
        active.setId(2L);
        active.setFirstName("Active");
        active.setEmail("active@example.com");
        active.setRole(Role.USER);
        active.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepo.findByEmail("active@example.com")).thenReturn(Optional.of(active));
        when(jwtUtils.generateToken(active)).thenReturn("token");

        AuthResponse response = service.login(dto);

        assertEquals("token", response.getToken());
        verify(accountStatusService).assertActive(active);
        verify(authenticationManager).authenticate(any());
    }
}
