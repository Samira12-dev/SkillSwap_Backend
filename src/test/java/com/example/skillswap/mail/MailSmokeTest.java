package com.example.skillswap.mail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MailSmokeTest {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.test-recipient:}")
    private String testRecipient;

    @Value("${app.email.from:}")
    private String configuredFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Test
    void sendsSinglePlainTextTestEmail() {
        assertTrue(StringUtils.hasText(testRecipient),
                "Test recipient is missing. Set MAIL_TEST_RECIPIENT "
                        + "(mapped to app.email.test-recipient).");

        String sender = StringUtils.hasText(configuredFrom)
                ? configuredFrom
                : mailUsername;
        assertTrue(StringUtils.hasText(sender),
                "Sender is missing. Set MAIL_FROM or MAIL_USERNAME "
                        + "(mapped to app.email.from).");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(testRecipient.trim());
        message.setSubject("SkillSwap SMTP smoke test");
        message.setText("This is a one-off SkillSwap SMTP smoke test. No action is required.");

        mailSender.send(message);
    }
}
