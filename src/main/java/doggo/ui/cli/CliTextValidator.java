package doggo.ui.cli;

import doggo.domain.Plan;
import doggo.domain.Trip;

/**
 * Validates text values entered through the CLI.
 */
final class CliTextValidator {
    private CliTextValidator() {
    }

    /**
     * Returns a validation message for a Trip title.
     *
     * @param title Trip title to validate.
     * @return Validation message, or an empty string when the title is valid.
     */
    static String validateTripTitle(String title) {
        if (title == null || title.isBlank()) {
            return "Trip title cannot be blank.";
        }
        if (exceedsLimit(title, Trip.MAX_TITLE_LENGTH)) {
            return "Trip title cannot exceed " + Trip.MAX_TITLE_LENGTH + " characters.";
        }
        return "";
    }

    /**
     * Returns a validation message for a Plan destination.
     *
     * @param destination Plan destination to validate.
     * @return Validation message, or an empty string when the destination is valid.
     */
    static String validatePlanDestination(String destination) {
        if (destination == null || destination.isBlank()) {
            return "Plan destination cannot be blank.";
        }
        if (exceedsLimit(destination, Plan.MAX_DESTINATION_LENGTH)) {
            return "Plan destination cannot exceed " + Plan.MAX_DESTINATION_LENGTH + " characters.";
        }
        return "";
    }

    /**
     * Checks whether text exceeds a maximum Unicode code point count.
     *
     * @param value Text to measure.
     * @param maximumLength Maximum allowed code point count.
     * @return True when the text exceeds the maximum length.
     */
    private static boolean exceedsLimit(String value, int maximumLength) {
        return value.codePointCount(0, value.length()) > maximumLength;
    }
}
