import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CliFormatterTest {
    @Test
    void tripView_planTimeIncludesSeconds_displaysHoursAndMinutesOnly() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0, 30));

        String output = new CliFormatter().tripView(trip, List.of(plan));

        assertTrue(output.contains("Mount Fuji (05/01/2027 at 09:00)"));
        assertFalse(output.contains("09:00:30"));
    }
}
