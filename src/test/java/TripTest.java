import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TripTest {
    @Test
    void createTrip_endBeforeStart_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Trip(
                UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 9), LocalDate.of(2027, 1, 1)));
    }
}
