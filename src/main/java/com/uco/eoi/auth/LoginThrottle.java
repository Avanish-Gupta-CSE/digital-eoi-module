package com.uco.eoi.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Stops one-time-password guessing on the dev/UAT login: after 5 wrong tries from one
 * address the address is locked for 15 minutes. A right password clears the count.
 */
@Component
public class LoginThrottle {

    static final int MAX_FAILURES = 5;
    static final Duration LOCK_FOR = Duration.ofMinutes(15);

    private record Failures(int count, Instant lockedUntil) {
    }

    private final ConcurrentHashMap<String, Failures> byAddress = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginThrottle() {
        this(Clock.systemUTC());
    }

    LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    /** True while this address is locked out. */
    public boolean isLocked(String address) {
        Failures failures = byAddress.get(address);
        if (failures == null || failures.lockedUntil() == null) {
            return false;
        }
        if (clock.instant().isBefore(failures.lockedUntil())) {
            return true;
        }
        byAddress.remove(address);
        return false;
    }

    /** Counts one wrong password; the fifth locks the address. */
    public void recordFailure(String address) {
        byAddress.merge(address, new Failures(1, null), (old, one) -> {
            int count = old.count() + 1;
            return new Failures(count,
                    count >= MAX_FAILURES ? clock.instant().plus(LOCK_FOR) : null);
        });
    }

    /** A right password clears the count. */
    public void recordSuccess(String address) {
        byAddress.remove(address);
    }
}
