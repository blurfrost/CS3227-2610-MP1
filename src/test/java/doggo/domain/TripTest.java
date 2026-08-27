package doggo.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
        Trip updatedTrip = trip.withAddedPlan(new Plan(UUID.randomUUID(), "Mount Fuji",
                LocalDate.of(2027, 1, 9), LocalTime.of(9, 0)));

        assertEquals(0, trip.plans().size());
        assertEquals(1, updatedTrip.plans().size());
    }

    @Test
    void addPlan_outsideDates_throwsException() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertThrows(IllegalArgumentException.class, () -> trip.withAddedPlan(new Plan(
                UUID.randomUUID(), "Tokyo", LocalDate.of(2027, 1, 10), LocalTime.of(9, 0))));
    }

    @Test
    void removePlan_returnsCopyWithoutSelectedPlan() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Trip tripWithPlan = trip.withAddedPlan(plan);

        Trip updatedTrip = tripWithPlan.withRemovedPlan(plan.id());

        assertEquals(1, tripWithPlan.plans().size());
        assertEquals(0, updatedTrip.plans().size());
    }

    @Test
    void updateDetails_excludingPlan_throwsException() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Trip tripWithPlan = trip.withAddedPlan(plan);

        assertThrows(IllegalArgumentException.class, () -> tripWithPlan.withUpdatedDetails(
                "Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 4)));
    }

    @Test
    void replacePlan_returnsCopyWithReplacementAndPreservesIdentities() {
        UUID tripId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        Trip trip = new Trip(tripId, "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan originalPlan = new Plan(planId, "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Plan replacement = new Plan(planId, "Osaka", LocalDate.of(2027, 1, 6),
                LocalTime.of(10, 0));

        Trip updatedTrip = trip.withAddedPlan(originalPlan).withReplacedPlan(replacement);

        assertEquals(tripId, updatedTrip.id());
        assertEquals(List.of(replacement), updatedTrip.plans());
        assertEquals(List.of(originalPlan), trip.withAddedPlan(originalPlan).plans());
    }
}
