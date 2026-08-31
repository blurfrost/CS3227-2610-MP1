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
        return validate(title, startDate, endDate, null, null, null);
    }

    /**
     * Returns a validation message for Trip fields and an optional existing Plan date range.
     *
     * @param title Trip title.
     * @param startDate Inclusive Trip start date.
     * @param endDate Inclusive Trip end date.
     * @param earliestPlanDate Earliest existing Plan date, or null when there are no Plans.
     * @param latestPlanDate Latest existing Plan date, or null when there are no Plans.
     * @param existingTitle Existing Trip title allowed to remain over the new-name limit, or null for creation.
     * @return Validation message, or an empty string when the fields are valid.
     */
    static String validate(String title, LocalDate startDate, LocalDate endDate,
                           LocalDate earliestPlanDate, LocalDate latestPlanDate, String existingTitle) {
        if (title == null || title.isBlank()) {
            return "Enter a name for your trip.";
        }
        boolean isUnchangedExistingTitle = existingTitle != null && title.trim().equals(existingTitle);
        if (!isUnchangedExistingTitle
                && title.trim().codePointCount(0, title.trim().length()) > Trip.MAX_TITLE_LENGTH) {
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
        if (earliestPlanDate != null && startDate.isAfter(earliestPlanDate)) {
            return "The start date cannot be after an existing plan.";
        }
        if (latestPlanDate != null && endDate.isBefore(latestPlanDate)) {
            return "The end date cannot be before an existing plan.";
        }
        return "";
    }
}
