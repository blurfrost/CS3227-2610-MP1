package doggo.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents one scheduled destination in a Trip itinerary.
 */
public final class Plan {
    private final UUID id;
    private final String destination;
    private final LocalDate date;
    private final LocalTime time;
    private final Optional<Review> review;

    /**
     * Creates a Plan with the specified identity, destination, date, and time.
     *
     * @param id Plan identity.
     * @param destination Plan destination.
     * @param date Plan date.
     * @param time Plan time.
     */
    public Plan(UUID id, String destination, LocalDate date, LocalTime time) {
        this(id, destination, date, time, Optional.empty());
    }

    private Plan(UUID id, String destination, LocalDate date, LocalTime time, Optional<Review> review) {
        this.id = Objects.requireNonNull(id);
        this.destination = requireText(destination, "Plan destination");
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.review = Objects.requireNonNull(review);
    }

    public UUID id() {
        return id;
    }

    public String destination() {
        return destination;
    }

    public LocalDate date() {
        return date;
    }

    public LocalTime time() {
        return time;
    }

    public Optional<Review> review() {
        return review;
    }

    /**
     * Returns a copy with updated Plan details.
     *
     * @param updatedDestination Updated Plan destination.
     * @param updatedDate Updated Plan date.
     * @param updatedTime Updated Plan time.
     * @return Copy of this Plan with updated details.
     */
    public Plan withUpdatedDetails(String updatedDestination, LocalDate updatedDate,
                                   LocalTime updatedTime) {
        return new Plan(id, updatedDestination, updatedDate, updatedTime, review);
    }

    /**
     * Returns a copy with the specified review attached.
     *
     * @param review Review to attach.
     * @return Copy of this Plan with the review attached.
     */
    public Plan withReview(Review review) {
        return new Plan(id, destination, date, time, Optional.of(Objects.requireNonNull(review)));
    }

    /**
     * Returns a copy without this Plan's review.
     *
     * @return Copy of this Plan without a review.
     */
    public Plan withoutReview() {
        return new Plan(id, destination, date, time, Optional.empty());
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value.trim();
    }
}
