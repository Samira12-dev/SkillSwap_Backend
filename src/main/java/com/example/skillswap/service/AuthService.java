package com.example.skillswap.service;
import com.example.skillswap.exception.BadRequestException;

import com.example.skillswap.config.JwtUtils;
import com.example.skillswap.dto.request.LoginRequestDTO;
import com.example.skillswap.dto.request.RegisterRequestDTO;
import com.example.skillswap.dto.response.AuthResponse;
import com.example.skillswap.dto.response.RegistrationResponse;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AccountStatus;
import com.example.skillswap.enums.Role;
import com.example.skillswap.exception.EmailNotVerifiedException;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int LOGIN_MAX_ATTEMPTS = 5;
    private static final int REGISTER_MAX_ATTEMPTS = 3;
    private static final int RESEND_MAX_ATTEMPTS = 3;
    private static final Duration AUTH_ATTEMPT_WINDOW = Duration.ofMinutes(15);
    private static final Duration HOURLY_ATTEMPT_WINDOW = Duration.ofHours(1);

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AccountStatusService accountStatusService;
    private final EmailVerificationService emailVerificationService;
    private final RateLimitService rateLimitService;

    public RegistrationResponse register(RegisterRequestDTO request) {

        String email = normalizeEmail(request.getEmail());
        String phone = normalizePhone(request.getPhone());

        rateLimitService.consume(
                "register:" + email,
                REGISTER_MAX_ATTEMPTS,
                HOURLY_ATTEMPT_WINDOW
        );

        if (userRepo.existsByEmail(email) || (phone != null && userRepo.existsByPhone(phone))) {
            return registrationResponse();
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(email);
        user.setPhone(phone);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCity(request.getCity());
        user.setBio(request.getBio());
        user.setPhoto(request.getPhoto());
        user.setRole(Role.USER);

        user = userRepo.save(user);
        emailVerificationService.issueVerificationToken(user);

        return registrationResponse();
    }

    private RegistrationResponse registrationResponse() {
        return new RegistrationResponse(
                "If the submitted details are valid, check your email to verify your address."
        );
    }

    public AuthResponse login(LoginRequestDTO request) {

        String email = normalizeEmail(request.getEmail());

        rateLimitService.consume(
                "login:" + email,
                LOGIN_MAX_ATTEMPTS,
                AUTH_ATTEMPT_WINDOW
        );

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        accountStatusService.assertActive(user);

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before signing in."
            );
        }

        String token = jwtUtils.generateToken(user);
        rateLimitService.reset("login:" + email);

        return new AuthResponse(
                token,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCity(),
                user.getPhoto(),
                user.getRole()
        );
    }

    @Transactional
    public void logoutAll(String email) {
        User user = userRepo.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepo.save(user);
    }

    public RegistrationResponse verifyEmail(String token) {
        emailVerificationService.verifyEmail(token);
        return new RegistrationResponse(
                "Email verified. You can now sign in."
        );
    }

    public RegistrationResponse resendVerificationEmail(String email) {
        rateLimitService.consume(
                "resend:" + normalizeEmail(email),
                RESEND_MAX_ATTEMPTS,
                HOURLY_ATTEMPT_WINDOW
        );

        emailVerificationService.resendVerificationEmail(email);
        return new RegistrationResponse(
                "If an unverified account exists for that email, a new verification message has been sent."
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        return phone.trim().replaceAll("[\\s().-]", "");
    }
}
