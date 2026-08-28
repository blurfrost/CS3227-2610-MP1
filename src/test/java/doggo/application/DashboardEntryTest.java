package doggo.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import doggo.domain.Plan;

import org.junit.jupiter.api.Test;

class DashboardEntryTest {
    private static final UUID TRIP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PLAN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final Plan PLAN = new Plan(PLAN_ID, "Museum", LocalDate.of(2027, 1, 5),
            LocalTime.of(9, 0));

    @Test
    void createEntry_trimsTripTitleAndRetainsComponents() {
        DashboardEntry entry = new DashboardEntry(TRIP_ID, " Japan ", PLAN);

        assertEquals(TRIP_ID, entry.tripId());
        assertEquals("Japan", entry.tripTitle());
        assertEquals(PLAN, entry.plan());
    }

    @Test
    void createEntry_nullComponent_throwsException() {
        assertThrows(NullPointerException.class, () -> new DashboardEntry(null, "Japan", PLAN));
        assertThrows(NullPointerException.class, () -> new DashboardEntry(TRIP_ID, null, PLAN));
        assertThrows(NullPointerException.class, () -> new DashboardEntry(TRIP_ID, "Japan", null));
    }

    @Test
    void createEntry_blankTripTitle_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new DashboardEntry(TRIP_ID, "   ", PLAN));
    }
}
