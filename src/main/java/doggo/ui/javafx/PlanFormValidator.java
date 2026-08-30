package doggo.ui.javafx;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Validates fields entered when creating or editing a Plan.
 */
final class PlanFormValidator {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private PlanFormValidator() {
    }

    /**
     * Returns a validation message for the specified Plan fields.
     *
     * @param destination Plan destination.
     * @param date Plan date.
     * @param timeText Plan time in HH:mm format.
     * @param tripStartDate Inclusive Trip start date.
     * @param tripEndDate Inclusive Trip end date.
     * @return Validation message, or an empty string when the fields are valid.
     */
    static String validate(String destination, LocalDate date, String timeText,
                           LocalDate tripStartDate, LocalDate tripEndDate) {
        if (destination == null || destination.isBlank()) {
            return "Enter a destination.";
        }
        if (date == null) {
            return "Choose a date for this plan.";
        }
        if (timeText == null || timeText.isBlank()) {
            return "Enter a time in HH:mm format.";
        }
        try {
            parseTime(timeText);
        } catch (IllegalArgumentException exception) {
            return "Enter a time in HH:mm format.";
        }
        if (date.isBefore(tripStartDate) || date.isAfter(tripEndDate)) {
            return "The plan date must be within the trip dates.";
        }
        return "";
    }

    /**
     * Parses a Plan time in the strict HH:mm format.
     *
     * @param timeText Time text to parse.
     * @return Parsed local time.
     * @throws IllegalArgumentException If the text is not a valid HH:mm time.
     */
    static LocalTime parseTime(String timeText) {
        try {
            return LocalTime.parse(timeText.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Time must use HH:mm format.", exception);
        }
    }
}
