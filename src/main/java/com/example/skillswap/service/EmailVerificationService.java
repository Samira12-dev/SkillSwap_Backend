package com.example.skillswap.service;

import com.example.skillswap.entity.User;
import com.example.skillswap.exception.EmailDeliveryException;
import com.example.skillswap.exception.EmailVerificationException;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepo userRepo;
    private final JavaMailSender mailSender;

    @Value("${app.email.verification-expiration-ms:86400000}")
    private long verificationExpirationMs;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.email.from}")
    private String fromAddress;

    @Transactional
    public void issueVerificationToken(User user) {
        String rawToken = generateToken();

        user.setEmailVerificationTokenHash(hashToken(rawToken));
        user.setEmailVerificationExpiresAt(
                LocalDateTime.now().plusNanos(verificationExpirationMs * 1_000_000)
        );

        userRepo.save(user);
        sendVerificationEmail(user.getEmail(), rawToken);
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw invalidToken();
        }

        User user = userRepo.findByEmailVerificationTokenHash(hashToken(rawToken))
                .orElseThrow(this::invalidToken);

        if (user.getEmailVerificationExpiresAt() == null
                || !user.getEmailVerificationExpiresAt().isAfter(LocalDateTime.now())) {
            throw invalidToken();
        }

        user.setEmailVerified(true);
        user.setEmailVerificationTokenHash(null);
        user.setEmailVerificationExpiresAt(null);
        userRepo.save(user);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        String normalizedEmail = email == null
                ? null
                : email.trim().toLowerCase(Locale.ROOT);

        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return;
        }

        userRepo.findByEmail(normalizedEmail)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::issueVerificationToken);
    }

    private void sendVerificationEmail(String email, String rawToken) {
        String verificationUrl = frontendUrl.replaceAll("/+$", "")
                + "/verify-email?token=" + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Verify your SkillSwap email");
        message.setText(
                "Welcome to SkillSwap.\n\n"
                        + "Verify your email by opening this link:\n"
                        + verificationUrl
                        + "\n\nIf you did not create this account, you can ignore this email."
        );

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error(
                    "Could not send email verification message (failure type: {})",
                    ex.getClass().getSimpleName()
            );
            throw new EmailDeliveryException(
                    "The verification email could not be sent. Please try again later.",
                    ex
            );
        }
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private EmailVerificationException invalidToken() {
        return new EmailVerificationException("Invalid or expired verification link.");
    }
}
