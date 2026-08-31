package doggo.ui.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class TripFormValidatorTest {
    private static final LocalDate START_DATE = LocalDate.of(2027, 1, 5);
    private static final LocalDate END_DATE = LocalDate.of(2027, 1, 9);

    @Test
    void validate_blankTitle_rejectsInput() {
        assertEquals("Enter a name for your trip.",
                TripFormValidator.validate("  ", START_DATE, END_DATE));
    }

    @Test
    void validate_titleAtLimit_acceptsInput() {
        String title = "旅".repeat(Trip.MAX_TITLE_LENGTH);

        assertEquals("", TripFormValidator.validate(title, START_DATE, END_DATE));
    }

    @Test
    void validate_titleBeyondLimit_rejectsInput() {
        String title = "旅".repeat(Trip.MAX_TITLE_LENGTH + 1);

        assertEquals("Trip name must be " + Trip.MAX_TITLE_LENGTH + " characters or fewer.",
                TripFormValidator.validate(title, START_DATE, END_DATE));
    }

    @Test
    void validate_missingStartDate_rejectsInput() {
        assertEquals("Choose a start date.",
                TripFormValidator.validate("Japan", null, END_DATE));
    }

    @Test
    void validate_missingEndDate_rejectsInput() {
        assertEquals("Choose an end date.",
                TripFormValidator.validate("Japan", START_DATE, null));
    }

    @Test
    void validate_endBeforeStart_rejectsInput() {
        assertEquals("The end date cannot be before the start date.",
                TripFormValidator.validate("Japan", END_DATE, START_DATE));
    }

    @Test
    void validate_validFields_acceptsInput() {
        assertEquals("", TripFormValidator.validate("Japan", START_DATE, END_DATE));
    }
}
