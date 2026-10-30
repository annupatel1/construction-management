package com.construction.construction_management.security;

import com.construction.construction_management.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Small in-memory guard against password guessing. It is deliberately per-instance: for a
 * multi-instance deployment this should move to Redis or a database table.
 */
@Component
public class LoginAttemptService {

    private final int maxAttempts;
    private final Duration window;
    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public LoginAttemptService(@Value("${auth.login.max-attempts:10}") int maxAttempts,
                               @Value("${auth.login.window-seconds:900}") long windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    public void checkAllowed(String key) {
        prune(key);
        Deque<Instant> attempts = failures.get(key);
        if (attempts != null && attempts.size() >= maxAttempts) {
            throw new TooManyRequestsException("Too many login attempts. Please try again later.");
        }
    }

    public void recordFailure(String key) {
        Instant cutoff = Instant.now().minus(window);
        failures.compute(key, (ignored, attempts) -> {
            Deque<Instant> current = attempts == null ? new ConcurrentLinkedDeque<>() : attempts;
            while (!current.isEmpty() && current.peekFirst().isBefore(cutoff)) {
                current.removeFirst();
            }
            current.addLast(Instant.now());
            return current;
        });
    }

    public void clear(String key) {
        failures.remove(key);
    }

    /** Keeps the map bounded when attackers probe many different phone numbers. */
    @Scheduled(fixedDelay = 600_000)
    void purgeExpired() {
        Instant cutoff = Instant.now().minus(window);
        failures.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(instant -> instant.isBefore(cutoff));
            return entry.getValue().isEmpty();
        });
    }

    private void prune(String key) {
        Instant cutoff = Instant.now().minus(window);
        failures.computeIfPresent(key, (ignored, attempts) -> {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
                attempts.removeFirst();
            }
            return attempts.isEmpty() ? null : attempts;
        });
    }
}
