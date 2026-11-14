package com.example.skillswap.service;

import com.example.skillswap.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Deque<Instant>> attemptsByKey = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimitService() {
        this(Clock.systemUTC());
    }

    RateLimitService(Clock clock) {
        this.clock = clock;
    }

    public void consume(String key, int maxAttempts, Duration window) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }

        Instant now = Instant.now(clock);
        Instant cutoff = now.minus(window);
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(
                key,
                ignored -> new ArrayDeque<>()
        );

        synchronized (attempts) {
            while (!attempts.isEmpty() && !attempts.peekFirst().isAfter(cutoff)) {
                attempts.removeFirst();
            }

            if (attempts.size() >= maxAttempts) {
                throw new RateLimitExceededException(
                        "Too many attempts. Please try again later."
                );
            }

            attempts.addLast(now);
        }
    }

    public void reset(String key) {
        attemptsByKey.remove(key);
    }
}
