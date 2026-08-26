import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents one scheduled destination in a Trip itinerary.
 */
final class Plan {
    private final UUID id;
    private final String destination;
    private final LocalDate date;
    private final LocalTime time;

    /**
     * Creates a Plan with the specified identity, destination, date, and time.
     *
     * @param id Plan identity.
     * @param destination Plan destination.
     * @param date Plan date.
     * @param time Plan time.
     */
    Plan(UUID id, String destination, LocalDate date, LocalTime time) {
        this.id = Objects.requireNonNull(id);
        this.destination = requireText(destination, "Plan destination");
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
    }

    UUID id() {
        return id;
    }

    String destination() {
        return destination;
    }

    LocalDate date() {
        return date;
    }

    LocalTime time() {
        return time;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value.trim();
    }
}
