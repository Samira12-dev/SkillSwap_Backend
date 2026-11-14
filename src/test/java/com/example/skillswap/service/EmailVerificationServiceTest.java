package com.example.skillswap.service;

import com.example.skillswap.entity.User;
import com.example.skillswap.exception.EmailDeliveryException;
import com.example.skillswap.exception.EmailVerificationException;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private JavaMailSender mailSender;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(userRepo, mailSender);
        ReflectionTestUtils.setField(service, "verificationExpirationMs", 86_400_000L);
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5173/");
        ReflectionTestUtils.setField(service, "fromAddress", "sender@example.com");
    }

    @Test
    void issueTokenSendsLinkAndStoresOnlyHash() {
        User user = user("user@example.com", false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.issueVerificationToken(user);

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        String rawToken = extractToken(messageCaptor.getValue().getText());
        User saved = user;

        assertNotNull(saved.getEmailVerificationTokenHash());
        assertEquals(64, saved.getEmailVerificationTokenHash().length());
        assertNotEquals(rawToken, saved.getEmailVerificationTokenHash());
        assertEquals(
                EmailVerificationService.hashToken(rawToken),
                saved.getEmailVerificationTokenHash()
        );
        assertTrue(saved.getEmailVerificationExpiresAt().isAfter(LocalDateTime.now()));
        assertEquals("user@example.com", messageCaptor.getValue().getTo()[0]);
        assertEquals("sender@example.com", messageCaptor.getValue().getFrom());
        assertTrue(messageCaptor.getValue().getText()
                .contains("http://localhost:5173/verify-email?token="));
    }

    @Test
    void emailDeliveryFailureIsPropagated() {
        User user = user("user@example.com", false);
        MailSendException smtpFailure = new MailSendException("SMTP unavailable");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doThrow(smtpFailure)
                .when(mailSender).send(any(SimpleMailMessage.class));

        EmailDeliveryException exception = assertThrows(
                EmailDeliveryException.class,
                () -> service.issueVerificationToken(user)
        );

        assertEquals(
                "The verification email could not be sent. Please try again later.",
                exception.getMessage()
        );
        assertEquals(smtpFailure, exception.getCause());
    }

    @Test
    void verifyEmailMarksUserVerifiedAndClearsToken() {
        User user = user("user@example.com", false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.issueVerificationToken(user);

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        String rawToken = extractToken(messageCaptor.getValue().getText());

        when(userRepo.findByEmailVerificationTokenHash(user.getEmailVerificationTokenHash()))
                .thenReturn(Optional.of(user));

        service.verifyEmail(rawToken);

        assertTrue(user.isEmailVerified());
        assertNull(user.getEmailVerificationTokenHash());
        assertNull(user.getEmailVerificationExpiresAt());
        verify(userRepo, times(2)).save(user);

        when(userRepo.findByEmailVerificationTokenHash(
                EmailVerificationService.hashToken(rawToken)
        )).thenReturn(Optional.empty());
        assertThrows(EmailVerificationException.class, () -> service.verifyEmail(rawToken));
    }

    @Test
    void expiredTokenIsRejected() {
        User user = user("user@example.com", false);
        user.setEmailVerificationTokenHash(
                EmailVerificationService.hashToken("expired-token")
        );
        user.setEmailVerificationExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(userRepo.findByEmailVerificationTokenHash(
                EmailVerificationService.hashToken("expired-token")
        )).thenReturn(Optional.of(user));

        assertThrows(EmailVerificationException.class,
                () -> service.verifyEmail("expired-token"));
    }

    @Test
    void unknownTokenIsRejected() {
        when(userRepo.findByEmailVerificationTokenHash(
                EmailVerificationService.hashToken("unknown-token")
        )).thenReturn(Optional.empty());

        assertThrows(EmailVerificationException.class,
                () -> service.verifyEmail("unknown-token"));
    }

    @Test
    void resendIgnoresVerifiedAndUnknownAccounts() {
        User verified = user("verified@example.com", true);

        when(userRepo.findByEmail("verified@example.com"))
                .thenReturn(Optional.of(verified));
        when(userRepo.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        service.resendVerificationEmail("verified@example.com");
        service.resendVerificationEmail("unknown@example.com");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void resendNormalizesEmailAndReplacesPreviousToken() {
        User user = user("user@example.com", false);
        user.setEmailVerificationTokenHash(
                EmailVerificationService.hashToken("previous-token")
        );
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusHours(1));
        String oldHash = user.getEmailVerificationTokenHash();
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.resendVerificationEmail("  USER@Example.com ");

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(userRepo).findByEmail("user@example.com");
        verify(mailSender).send(messageCaptor.capture());
        String newToken = extractToken(messageCaptor.getValue().getText());

        assertNotEquals(oldHash, user.getEmailVerificationTokenHash());
        assertEquals(
                EmailVerificationService.hashToken(newToken),
                user.getEmailVerificationTokenHash()
        );
        assertTrue(user.getEmailVerificationExpiresAt().isAfter(LocalDateTime.now()));
        assertEquals("user@example.com", messageCaptor.getValue().getTo()[0]);

        when(userRepo.findByEmailVerificationTokenHash(oldHash))
                .thenReturn(Optional.empty());
        assertThrows(
                EmailVerificationException.class,
                () -> service.verifyEmail("previous-token")
        );
    }

    @Test
    void blankResendEmailDoesNothing() {
        service.resendVerificationEmail("  ");

        verifyNoInteractions(userRepo, mailSender);
    }

    private User user(String email, boolean emailVerified) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setEmailVerified(emailVerified);
        return user;
    }

    private String extractToken(String emailBody) {
        Matcher matcher = Pattern.compile("token=([A-Za-z0-9_-]+)").matcher(emailBody);
        assertTrue(matcher.find());
        return matcher.group(1);
    }
}
