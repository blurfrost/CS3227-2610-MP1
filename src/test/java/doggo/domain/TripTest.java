package doggo.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TripTest {
    private static final LocalDate CURRENT_DATE = LocalDate.of(2027, 1, 5);
    private static final Review REVIEW = new Review(OptionalInt.of(4), Optional.of("Great trip"));

    @Test
    void createTrip_endBeforeStart_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Trip(
                UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 9), LocalDate.of(2027, 1, 1)));
    }

    @Test
    void statusOn_pastTrip_returnsPast() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));

        assertEquals(TripStatus.PAST, trip.statusOn(CURRENT_DATE));
    }

    @Test
    void statusOn_futureTrip_returnsFuture() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 6),
                LocalDate.of(2027, 1, 9));

        assertEquals(TripStatus.FUTURE, trip.statusOn(CURRENT_DATE));
    }

    @Test
    void statusOn_tripSpanningCurrentDate_returnsCurrent() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertEquals(TripStatus.CURRENT, trip.statusOn(CURRENT_DATE));
    }

    @Test
    void statusOn_tripStartingOnCurrentDate_returnsCurrent() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", CURRENT_DATE, LocalDate.of(2027, 1, 9));

        assertEquals(TripStatus.CURRENT, trip.statusOn(CURRENT_DATE));
    }

    @Test
    void statusOn_tripEndingOnCurrentDate_returnsCurrent() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1), CURRENT_DATE);

        assertEquals(TripStatus.CURRENT, trip.statusOn(CURRENT_DATE));
    }

    @Test
    void statusOn_singleDayTripOnCurrentDate_returnsCurrent() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", CURRENT_DATE, CURRENT_DATE);

        assertEquals(TripStatus.CURRENT, trip.statusOn(CURRENT_DATE));
    }

    @Test
    void statusOn_nullCurrentDate_throwsException() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertThrows(NullPointerException.class, () -> trip.statusOn(null));
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

    @Test
    void review_defaultsToEmpty() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertEquals(Optional.empty(), trip.review());
    }

    @Test
    void withReview_returnsReviewedCopyWithoutChangingOriginal() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        Trip reviewedTrip = trip.withReview(REVIEW);

        assertEquals(Optional.empty(), trip.review());
        assertEquals(Optional.of(REVIEW), reviewedTrip.review());
    }

    @Test
    void withoutReview_returnsUnreviewedCopyWithoutChangingOriginal() {
        Trip reviewedTrip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9)).withReview(REVIEW);

        Trip unreviewedTrip = reviewedTrip.withoutReview();

        assertEquals(Optional.of(REVIEW), reviewedTrip.review());
        assertEquals(Optional.empty(), unreviewedTrip.review());
    }

    @Test
    void copyOperations_preserveTripReview() {
        UUID planId = UUID.randomUUID();
        Plan plan = new Plan(planId, "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));
        Trip reviewedTrip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9)).withReview(REVIEW);

        Trip withPlan = reviewedTrip.withAddedPlan(plan);
        Trip withReplacement = withPlan.withReplacedPlan(
                new Plan(planId, "Osaka", LocalDate.of(2027, 1, 6), LocalTime.of(10, 0)));
        Trip withoutPlan = withReplacement.withRemovedPlan(planId);
        Trip withUpdatedDetails = withoutPlan.withUpdatedDetails("Kyoto", LocalDate.of(2027, 1, 2),
                LocalDate.of(2027, 1, 8));

        assertEquals(Optional.of(REVIEW), withPlan.review());
        assertEquals(Optional.of(REVIEW), withReplacement.review());
        assertEquals(Optional.of(REVIEW), withoutPlan.review());
        assertEquals(Optional.of(REVIEW), withUpdatedDetails.review());
        assertEquals(Optional.of(REVIEW), reviewedTrip.review());
    }
}
