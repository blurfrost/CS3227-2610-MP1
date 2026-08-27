package doggo.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PlanTest {
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
}
