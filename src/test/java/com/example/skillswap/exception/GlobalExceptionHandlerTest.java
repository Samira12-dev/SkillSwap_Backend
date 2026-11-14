package com.example.skillswap.exception;

import com.example.skillswap.dto.response.ApiErrorResponse;
import com.example.skillswap.exception.EmailDeliveryException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedExceptionsDoNotExposeInternalMessages() {
        ResponseEntity<String> response = handler.handleRuntime(
                new RuntimeException("database password is secret")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred.", response.getBody());
        assertFalse(response.getBody().contains("secret"));
    }

    @Test
    void authenticationFailuresUseGenericUnauthorizedMessage() {
        ResponseEntity<String> response = handler.handleAuthentication(
                new BadCredentialsException("User user@example.com not found")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password.", response.getBody());
        assertFalse(response.getBody().contains("user@example.com"));
    }

    @Test
    void badRequestsPreserveSafeUserFacingMessages() {
        ResponseEntity<String> response = handler.handleBadRequest(
                new BadRequestException("You cannot send a swap request to yourself")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("You cannot send a swap request to yourself", response.getBody());
    }

    @Test
    void unverifiedEmailReturnsStructuredForbiddenResponse() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleEmailNotVerified(
                        new EmailNotVerifiedException(
                                "Please verify your email before signing in."
                        )
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("EMAIL_NOT_VERIFIED", response.getBody().code());
        assertEquals(
                "Please verify your email before signing in.",
                response.getBody().message()
        );
    }

    @Test
    void emailDeliveryFailureReturnsServiceUnavailableWithoutLeakingSmtpDetails() {
        ResponseEntity<String> response = handler.handleEmailDelivery(
                new EmailDeliveryException(
                        "The verification email could not be sent. Please try again later.",
                        new RuntimeException("SMTP authentication details")
                )
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(
                "The verification email could not be sent. Please try again later.",
                response.getBody()
        );
        assertFalse(response.getBody().contains("SMTP authentication details"));
    }
}
