package doggo.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class TripStatusTest {
    @Test
    void values_returnsStatusesInDeclaredOrder() {
        assertArrayEquals(new TripStatus[] {TripStatus.PAST, TripStatus.CURRENT, TripStatus.FUTURE},
                TripStatus.values());
    }
}
