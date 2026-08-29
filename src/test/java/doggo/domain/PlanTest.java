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
    void withoutReview_returnsUnreviewedCopyWithoutChangingOriginal() {
        Plan reviewedPlan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0)).withReview(REVIEW);

        Plan unreviewedPlan = reviewedPlan.withoutReview();

        assertEquals(Optional.of(REVIEW), reviewedPlan.review());
        assertEquals(Optional.empty(), unreviewedPlan.review());
    }

    @Test
    void withUpdatedDetails_preservesReviewWithoutChangingOriginal() {
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0)).withReview(REVIEW);

        Plan updatedPlan = plan.withUpdatedDetails("Osaka Castle", LocalDate.of(2027, 1, 6),
                LocalTime.of(10, 30));

        assertEquals("Mount Fuji", plan.destination());
        assertEquals(LocalDate.of(2027, 1, 5), plan.date());
        assertEquals(LocalTime.of(9, 0), plan.time());
        assertEquals(Optional.of(REVIEW), plan.review());
        assertEquals("Osaka Castle", updatedPlan.destination());
        assertEquals(LocalDate.of(2027, 1, 6), updatedPlan.date());
        assertEquals(LocalTime.of(10, 30), updatedPlan.time());
        assertEquals(Optional.of(REVIEW), updatedPlan.review());
    }
}
