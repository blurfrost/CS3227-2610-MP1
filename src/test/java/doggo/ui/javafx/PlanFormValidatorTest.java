package doggo.ui.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class PlanFormValidatorTest {
    private static final LocalDate TRIP_START_DATE = LocalDate.of(2027, 1, 5);
    private static final LocalDate TRIP_END_DATE = LocalDate.of(2027, 1, 9);

    @Test
    void validate_blankDestination_rejectsInput() {
        assertEquals("Enter a destination.", validate("  ", "09:00", TRIP_START_DATE));
    }

    @Test
    void validate_missingDate_rejectsInput() {
        assertEquals("Choose a date for this plan.", validate("Museum", "09:00", null));
    }

    @Test
    void validate_missingTime_rejectsInput() {
        assertEquals("Enter a time in HH:mm format.", validate("Museum", "  ", TRIP_START_DATE));
    }

    @Test
    void validate_invalidTime_rejectsInput() {
        assertEquals("Enter a time in HH:mm format.", validate("Museum", "25:00", TRIP_START_DATE));
    }

    @Test
    void validate_dateOutsideTrip_rejectsInput() {
        assertEquals("The plan date must be within the trip dates.",
                validate("Museum", "09:00", TRIP_END_DATE.plusDays(1)));
    }

    @Test
    void validate_validFields_acceptsInput() {
        assertEquals("", validate("Museum", "09:00", TRIP_START_DATE));
    }

    @Test
    void parseTime_validTime_returnsTime() {
        assertEquals(LocalTime.of(9, 0), PlanFormValidator.parseTime("09:00"));
    }

    private static String validate(String destination, String timeText, LocalDate date) {
        return PlanFormValidator.validate(destination, date, timeText,
                TRIP_START_DATE, TRIP_END_DATE);
    }
}
