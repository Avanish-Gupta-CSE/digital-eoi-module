package com.uco.eoi.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Five wrong passwords lock an address for 15 minutes. */
class LoginThrottleTest {

    @Test
    @DisplayName("the fifth wrong password locks the address, and the lock ends after 15 minutes")
    void fiveFailures_lockForFifteenMinutes() {
        Instant start = Instant.parse("2026-11-28T10:00:00Z");
        AtomicReference<Instant> now = new AtomicReference<>(start);
        Clock clock = new Clock() {
            public ZoneOffset getZone() { return ZoneOffset.UTC; }
            public Clock withZone(java.time.ZoneId zone) { return this; }
            public Instant instant() { return now.get(); }
        };
        LoginThrottle throttle = new LoginThrottle(clock);
        for (int i = 0; i < 4; i++) {
            throttle.recordFailure("10.0.0.1");
        }
        assertThat(throttle.isLocked("10.0.0.1")).isFalse();

        throttle.recordFailure("10.0.0.1");
        assertThat(throttle.isLocked("10.0.0.1")).isTrue();
        assertThat(throttle.isLocked("10.0.0.2")).isFalse();

        now.set(start.plusSeconds(901));
        assertThat(throttle.isLocked("10.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("a right password clears the count")
    void success_clearsTheCount() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < 4; i++) {
            throttle.recordFailure("10.0.0.1");
        }
        throttle.recordSuccess("10.0.0.1");
        throttle.recordFailure("10.0.0.1");

        assertThat(throttle.isLocked("10.0.0.1")).isFalse();
    }
}
