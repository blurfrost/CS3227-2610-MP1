package doggo.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TripTest {
    private static final LocalDate CURRENT_DATE = LocalDate.of(2027, 1, 5);
    private static final Review REVIEW = new Review(OptionalInt.of(4), Optional.of("Great trip"));

    @Test
    void createTrip_validValues_storesValues() {
        UUID tripId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2027, 1, 1);
        LocalDate endDate = LocalDate.of(2027, 1, 9);

        Trip trip = new Trip(tripId, "  Japan  ", startDate, endDate);

        assertEquals(tripId, trip.id());
        assertEquals("Japan", trip.title());
        assertEquals(startDate, trip.startDate());
        assertEquals(endDate, trip.endDate());
        assertEquals(List.of(), trip.plans());
    }

    @Test
    void createTrip_blankTitle_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Trip(
                UUID.randomUUID(), "  ", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9)));
    }

    @Test
    void createTrip_nullArgument_throwsException() {
        UUID tripId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2027, 1, 1);
        LocalDate endDate = LocalDate.of(2027, 1, 9);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new Trip(null, "Japan", startDate, endDate)),
                () -> assertThrows(NullPointerException.class,
                        () -> new Trip(tripId, null, startDate, endDate)),
                () -> assertThrows(NullPointerException.class,
                        () -> new Trip(tripId, "Japan", null, endDate)),
                () -> assertThrows(NullPointerException.class,
                        () -> new Trip(tripId, "Japan", startDate, null)));
    }

    @Test
    void createTrip_endBeforeStart_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Trip(
                UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 9), LocalDate.of(2027, 1, 1)));
    }

    @Test
    void restore_fullState_preservesIdentityReviewAndPlansAndTrimsTitle() {
        UUID tripId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2027, 1, 1);
        LocalDate endDate = LocalDate.of(2027, 1, 9);
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        Trip trip = Trip.restore(tripId, "  Japan  ", startDate, endDate, List.of(plan),
                Optional.of(REVIEW));

        assertAll(
                () -> assertEquals(tripId, trip.id()),
                () -> assertEquals("Japan", trip.title()),
                () -> assertEquals(startDate, trip.startDate()),
                () -> assertEquals(endDate, trip.endDate()),
                () -> assertEquals(List.of(plan), trip.plans()),
                () -> assertEquals(Optional.of(REVIEW), trip.review()));
    }

    @Test
    void restore_absentReviewAndEmptyPlans_preservesEmptyState() {
        Trip trip = Trip.restore(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9), List.of(), Optional.empty());

        assertAll(
                () -> assertEquals(List.of(), trip.plans()),
                () -> assertEquals(Optional.empty(), trip.review()));
    }

    @Test
    void restore_plans_areDefensivelyCopiedAndUnmodifiable() {
        List<Plan> plans = new ArrayList<>();
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        plans.add(plan);

        Trip trip = Trip.restore(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9), plans, Optional.empty());
        plans.clear();

        assertEquals(List.of(plan), trip.plans());
        assertThrows(UnsupportedOperationException.class, () -> trip.plans().add(plan));
    }

    @Test
    void restore_invalidOrNullState_throwsException() {
        UUID id = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2027, 1, 1);
        LocalDate endDate = LocalDate.of(2027, 1, 9);
        Plan validPlan = new Plan(UUID.randomUUID(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> Trip.restore(null, "Japan", startDate, endDate, List.of(),
                                Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Trip.restore(id, null, startDate, endDate, List.of(), Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Trip.restore(id, "Japan", null, endDate, List.of(), Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Trip.restore(id, "Japan", startDate, null, List.of(), Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Trip.restore(id, "Japan", startDate, endDate, null, Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Trip.restore(id, "Japan", startDate, endDate,
                                Arrays.asList((Plan) null), Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Trip.restore(id, "Japan", startDate, endDate, List.of(), null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Trip.restore(id, "Japan", LocalDate.of(2027, 1, 9),
                                startDate, List.of(), Optional.empty())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Trip.restore(id, "Japan", startDate, endDate,
                                List.of(validPlan.withUpdatedDetails(validPlan.destination(),
                                        LocalDate.of(2027, 1, 10), validPlan.time())), Optional.empty())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Trip.restore(id, "  ", startDate, endDate, List.of(), Optional.empty())));
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
    void addPlan_nullPlan_throwsException() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertThrows(NullPointerException.class, () -> trip.withAddedPlan(null));
    }

    @Test
    void plans_returnsUnmodifiableList() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertThrows(UnsupportedOperationException.class, () -> trip.plans().add(
                new Plan(UUID.randomUUID(), "Tokyo", LocalDate.of(2027, 1, 5),
                        LocalTime.of(9, 0))));
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
    void removePlan_missingPlan_throwsException() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> trip.withRemovedPlan(UUID.randomUUID())),
                () -> assertThrows(NullPointerException.class, () -> trip.withRemovedPlan(null)));
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
    void updateDetails_validValues_preservesIdentityAndPlans() {
        UUID tripId = UUID.randomUUID();
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Trip trip = new Trip(tripId, "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9)).withAddedPlan(plan);

        Trip updatedTrip = trip.withUpdatedDetails("Kyoto", LocalDate.of(2027, 1, 2),
                LocalDate.of(2027, 1, 8));

        assertEquals(tripId, updatedTrip.id());
        assertEquals("Kyoto", updatedTrip.title());
        assertEquals(List.of(plan), updatedTrip.plans());
        assertEquals("Japan", trip.title());
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
    void replacePlan_missingOrInvalidPlan_throwsException() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan replacement = new Plan(UUID.randomUUID(), "Osaka", LocalDate.of(2027, 1, 6),
                LocalTime.of(10, 0));

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> trip.withReplacedPlan(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> trip.withReplacedPlan(replacement)));

        Plan originalPlan = new Plan(UUID.randomUUID(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Plan outsidePlan = new Plan(originalPlan.id(), "Osaka", LocalDate.of(2027, 1, 10),
                LocalTime.of(10, 0));

        assertThrows(IllegalArgumentException.class,
                () -> trip.withAddedPlan(originalPlan).withReplacedPlan(outsidePlan));
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
    void withReview_nullReview_throwsException() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertThrows(NullPointerException.class, () -> trip.withReview(null));
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
