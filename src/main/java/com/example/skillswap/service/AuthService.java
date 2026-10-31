package com.example.skillswap.service;

import com.example.skillswap.config.JwtUtils;
import com.example.skillswap.dto.request.LoginRequestDTO;
import com.example.skillswap.dto.request.RegisterRequestDTO;
import com.example.skillswap.dto.response.AuthResponse;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AccountStatus;
import com.example.skillswap.enums.Role;
import com.example.skillswap.exception.ConflictException;
import com.example.skillswap.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AccountStatusService accountStatusService;

    public AuthResponse register(RegisterRequestDTO request) {

        String email = normalizeEmail(request.getEmail());
        String phone = normalizePhone(request.getPhone());

        if (userRepo.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }

        if (phone != null && userRepo.existsByPhone(phone)) {
            throw new ConflictException(
                    "This phone number is already associated with an existing account."
            );
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

        String token = jwtUtils.generateToken(user);

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

    public AuthResponse login(LoginRequestDTO request) {

        String email = normalizeEmail(request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        accountStatusService.assertActive(user);

        String token = jwtUtils.generateToken(user);

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
