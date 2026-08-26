import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TripTest {
    @Test
    void createTrip_endBeforeStart_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Trip(
                UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 9), LocalDate.of(2027, 1, 1)));
    }

    @Test
    void addPlan_withinInclusiveDates_addsPlan() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        trip.addPlan(new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 9),
                LocalTime.of(9, 0)));

        assertEquals(1, trip.plans().size());
    }

    @Test
    void addPlan_outsideDates_throwsException() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertThrows(IllegalArgumentException.class, () -> trip.addPlan(new Plan(
                UUID.randomUUID(), "Tokyo", LocalDate.of(2027, 1, 10), LocalTime.of(9, 0))));
    }
}
