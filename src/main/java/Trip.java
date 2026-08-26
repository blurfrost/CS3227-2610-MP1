import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an overall journey.
 */
final class Trip {
    private final UUID id;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;

    /**
     * Creates a Trip with the specified title and inclusive date range.
     *
     * @param id Trip identity.
     * @param title Trip title.
     * @param startDate Trip start date.
     * @param endDate Trip end date.
     */
    Trip(UUID id, String title, LocalDate startDate, LocalDate endDate) {
        this.id = Objects.requireNonNull(id);
        this.title = requireText(title, "Trip title");
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Trip end date cannot be before its start date.");
        }
    }

    UUID id() {
        return id;
    }

    String title() {
        return title;
    }

    LocalDate startDate() {
        return startDate;
    }

    LocalDate endDate() {
        return endDate;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value.trim();
    }
}
