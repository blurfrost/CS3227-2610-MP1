package doggo.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PlanTest {
    private static final Review REVIEW = new Review(OptionalInt.of(5), Optional.of("Worth revisiting"));

    @Test
    void createPlan_validValues_storesValues() {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2027, 1, 5);
        LocalTime time = LocalTime.of(9, 0);
        Plan plan = new Plan(id, "Mount Fuji", date, time);

        assertAll(
                () -> assertEquals(id, plan.id()),
                () -> assertEquals("Mount Fuji", plan.destination()),
                () -> assertEquals(date, plan.date()),
                () -> assertEquals(time, plan.time()));
    }

    @Test
    void createPlan_destinationWithWhitespace_trimsAndPreservesCasing() {
        Plan plan = new Plan(UUID.randomUUID(), "  Back  ", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        assertEquals("Back", plan.destination());
    }

    @Test
    void createPlan_blankDestination_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Plan(
                UUID.randomUUID(), "  ", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0)));
    }

    @Test
    void createPlan_nullArguments_throwException() {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2027, 1, 5);
        LocalTime time = LocalTime.of(9, 0);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new Plan(null, "Tokyo", date, time)),
                () -> assertThrows(NullPointerException.class,
                        () -> new Plan(id, null, date, time)),
                () -> assertThrows(NullPointerException.class,
                        () -> new Plan(id, "Tokyo", null, time)),
                () -> assertThrows(NullPointerException.class,
                        () -> new Plan(id, "Tokyo", date, null)));
    }

    @Test
    void restore_fullState_preservesIdentityReviewAndTrimsDestination() {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2027, 1, 5);
        LocalTime time = LocalTime.of(9, 0);

        Plan plan = Plan.restore(id, "  Mount Fuji  ", date, time, Optional.of(REVIEW));

        assertAll(
                () -> assertEquals(id, plan.id()),
                () -> assertEquals("Mount Fuji", plan.destination()),
                () -> assertEquals(date, plan.date()),
                () -> assertEquals(time, plan.time()),
                () -> assertEquals(Optional.of(REVIEW), plan.review()));
    }

    @Test
    void restore_absentReview_preservesUnreviewedState() {
        Plan plan = Plan.restore(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0), Optional.empty());

        assertEquals(Optional.empty(), plan.review());
    }

    @Test
    void restore_invalidOrNullState_throwsException() {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2027, 1, 5);
        LocalTime time = LocalTime.of(9, 0);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> Plan.restore(null, "Tokyo", date, time, Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Plan.restore(id, null, date, time, Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Plan.restore(id, "Tokyo", null, time, Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Plan.restore(id, "Tokyo", date, null, Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> Plan.restore(id, "Tokyo", date, time, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Plan.restore(id, "  ", date, time, Optional.empty())));
    }

    @Test
    void review_defaultsToEmpty() {
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        assertEquals(Optional.empty(), plan.review());
    }

    @Test
    void withReview_returnsReviewedCopyWithoutChangingOriginal() {
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        Plan reviewedPlan = plan.withReview(REVIEW);

        assertEquals(Optional.empty(), plan.review());
        assertEquals(Optional.of(REVIEW), reviewedPlan.review());
    }

    @Test
    void withReview_nullReview_throwsException() {
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        assertThrows(NullPointerException.class, () -> plan.withReview(null));
    }

    @Test
    void withoutReview_returnsUnreviewedCopyWithoutChangingOriginal() {
        Plan reviewedPlan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0)).withReview(REVIEW);

        Plan unreviewedPlan = reviewedPlan.withoutReview();

        assertEquals(Optional.of(REVIEW), reviewedPlan.review());
        assertEquals(Optional.empty(), unreviewedPlan.review());
    }

    @Test
    void withUpdatedDetails_preservesReviewWithoutChangingOriginal() {
        UUID planId = UUID.randomUUID();
        Plan plan = new Plan(planId, "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0)).withReview(REVIEW);

        Plan updatedPlan = plan.withUpdatedDetails("Osaka Castle", LocalDate.of(2027, 1, 6),
                LocalTime.of(10, 30));

        assertEquals(planId, updatedPlan.id());
        assertEquals(planId, plan.id());
        assertEquals("Mount Fuji", plan.destination());
        assertEquals(LocalDate.of(2027, 1, 5), plan.date());
        assertEquals(LocalTime.of(9, 0), plan.time());
        assertEquals(Optional.of(REVIEW), plan.review());
        assertEquals("Osaka Castle", updatedPlan.destination());
        assertEquals(LocalDate.of(2027, 1, 6), updatedPlan.date());
        assertEquals(LocalTime.of(10, 30), updatedPlan.time());
        assertEquals(Optional.of(REVIEW), updatedPlan.review());
    }

    @Test
    void withUpdatedDetails_nullArgument_throwsException() {
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> plan.withUpdatedDetails(null, plan.date(), plan.time())),
                () -> assertThrows(NullPointerException.class,
                        () -> plan.withUpdatedDetails(plan.destination(), null, plan.time())),
                () -> assertThrows(NullPointerException.class,
                        () -> plan.withUpdatedDetails(plan.destination(), plan.date(), null)));
    }
}
