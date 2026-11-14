package com.example.skillswap.dto;

import com.example.skillswap.dto.request.LoginRequestDTO;
import com.example.skillswap.dto.request.RegisterRequestDTO;
import com.example.skillswap.dto.request.ResendVerificationRequest;
import com.example.skillswap.dto.request.VerifyEmailRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRequestValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void invalidRegistrationIsRejected() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setFirstName(" ");
        request.setLastName("");
        request.setEmail("not-an-email");
        request.setPassword("short");
        request.setCity("");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void validRegistrationPassesValidation() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("user@example.com");
        request.setPassword("password123");
        request.setCity("Paris");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void invalidLoginIsRejected() {
        LoginRequestDTO request = new LoginRequestDTO("not-an-email", "");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void invalidVerificationRequestsAreRejected() {
        assertFalse(validator.validate(new VerifyEmailRequest(" ")).isEmpty());
        assertFalse(validator.validate(
                new ResendVerificationRequest("not-an-email")
        ).isEmpty());
    }
}
