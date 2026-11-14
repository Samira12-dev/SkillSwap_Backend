package com.example.skillswap.service;

import com.example.skillswap.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateLimitServiceTest {

    @Test
    void allowsAttemptsUpToLimitThenRejects() {
        RateLimitService service = new RateLimitService();

        service.consume("login:user@example.com", 2, Duration.ofMinutes(15));
        service.consume("login:user@example.com", 2, Duration.ofMinutes(15));

        assertThrows(
                RateLimitExceededException.class,
                () -> service.consume(
                        "login:user@example.com",
                        2,
                        Duration.ofMinutes(15)
                )
        );
    }

    @Test
    void resetClearsPreviousAttempts() {
        RateLimitService service = new RateLimitService();

        service.consume("login:user@example.com", 1, Duration.ofMinutes(15));
        service.reset("login:user@example.com");

        assertDoesNotThrow(() -> service.consume(
                "login:user@example.com",
                1,
                Duration.ofMinutes(15)
        ));
    }

    @Test
    void oldAttemptsExpireOutsideWindow() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimitService service = new RateLimitService(clock);

        service.consume("login:user@example.com", 1, Duration.ofMinutes(15));
        clock.advance(Duration.ofMinutes(16));

        assertDoesNotThrow(() -> service.consume(
                "login:user@example.com",
                1,
                Duration.ofMinutes(15)
        ));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
