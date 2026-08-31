package doggo.ui.javafx;

import java.time.LocalDate;

import doggo.domain.Trip;

/**
 * Validates the fields entered when creating a Trip.
 */
final class TripFormValidator {
    private TripFormValidator() {
    }

    /**
     * Returns a validation message for the specified Trip fields.
     *
     * @param title Trip title.
     * @param startDate Inclusive Trip start date.
     * @param endDate Inclusive Trip end date.
     * @return Validation message, or an empty string when the fields are valid.
     */
    static String validate(String title, LocalDate startDate, LocalDate endDate) {
        if (title == null || title.isBlank()) {
            return "Enter a name for your trip.";
        }
        if (title.trim().codePointCount(0, title.trim().length()) > Trip.MAX_TITLE_LENGTH) {
            return "Trip name must be " + Trip.MAX_TITLE_LENGTH + " characters or fewer.";
        }
        if (startDate == null) {
            return "Choose a start date.";
        }
        if (endDate == null) {
            return "Choose an end date.";
        }
        if (endDate.isBefore(startDate)) {
            return "The end date cannot be before the start date.";
        }
        return "";
    }
}
