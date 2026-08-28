package doggo;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public final class TestClock {
    private TestClock() {
    }

    public static Clock fixed() {
        return Clock.fixed(Instant.parse("2027-01-05T00:00:00Z"), ZoneOffset.UTC);
    }
}
