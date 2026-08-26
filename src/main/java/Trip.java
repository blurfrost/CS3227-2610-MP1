import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an overall journey and its itinerary Plans.
 */
final class Trip {
    private final UUID id;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<Plan> plans = new ArrayList<>();

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

    List<Plan> plans() {
        return List.copyOf(plans);
    }

    /**
     * Adds a Plan when its date falls within this Trip's inclusive date range.
     *
     * @param plan Plan to add.
     */
    void addPlan(Plan plan) {
        Objects.requireNonNull(plan);
        if (plan.date().isBefore(startDate) || plan.date().isAfter(endDate)) {
            throw new IllegalArgumentException("Plan date must fall within the Trip dates.");
        }
        plans.add(plan);
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value.trim();
    }
}
