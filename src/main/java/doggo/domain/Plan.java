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
    /**
     * Maximum number of Unicode code points allowed in a new Plan destination.
     */
    public static final int MAX_DESTINATION_LENGTH = 50;

    private final UUID id;
    private final String destination;
    private final LocalDate date;
    private final LocalTime time;
    private final Optional<Review> review;
    /**
     * Whether this Plan was restored with a destination exceeding the current limit.
     */
    private final boolean hasLegacyDestination;

    /**
     * Creates a Plan with the specified identity, destination, date, and time.
     *
     * @param id Plan identity.
     * @param destination Plan destination.
     * @param date Plan date.
     * @param time Plan time.
     */
    public Plan(UUID id, String destination, LocalDate date, LocalTime time) {
        this(id, destination, date, time, Optional.empty(), false);
    }

    private Plan(UUID id, String destination, LocalDate date, LocalTime time, Optional<Review> review,
                 boolean isLegacyDestinationAllowed) {
        this.id = Objects.requireNonNull(id);
        this.destination = requireText(destination, "Plan destination", isLegacyDestinationAllowed);
        this.hasLegacyDestination = isLegacyDestinationAllowed
                && exceedsDestinationLimit(this.destination);
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.review = Objects.requireNonNull(review);
    }

    /**
     * Restores a Plan with its complete persisted state.
     *
     * @param id Plan identity.
     * @param destination Plan destination.
     * @param date Plan date.
     * @param time Plan time.
     * @param review Optional Plan review.
     * @return Restored Plan.
     * @throws NullPointerException If any argument is null.
     * @throws IllegalArgumentException If the destination is blank.
     */
    public static Plan restore(UUID id, String destination, LocalDate date, LocalTime time,
                               Optional<Review> review) {
        return new Plan(id, destination, date, time, review, true);
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
        boolean preserveLegacyDestination = hasLegacyDestination
                && destination.equals(Objects.requireNonNull(updatedDestination).trim());
        return new Plan(id, updatedDestination, updatedDate, updatedTime, review,
                preserveLegacyDestination);
    }

    /**
     * Returns a copy with the specified review attached.
     *
     * @param review Review to attach.
     * @return Copy of this Plan with the review attached.
     */
    public Plan withReview(Review review) {
        return new Plan(id, destination, date, time, Optional.of(Objects.requireNonNull(review)),
                hasLegacyDestination);
    }

    /**
     * Returns a copy without this Plan's review.
     *
     * @return Copy of this Plan without a review.
     */
    public Plan withoutReview() {
        return new Plan(id, destination, date, time, Optional.empty(), hasLegacyDestination);
    }

    /**
     * Validates and trims a Plan destination.
     *
     * @param value Destination to validate.
     * @param fieldName Name used in validation errors.
     * @param isLegacyDestinationAllowed Whether an over-limit restored destination is allowed.
     * @return Trimmed destination.
     */
    private static String requireText(String value, String fieldName,
                                      boolean isLegacyDestinationAllowed) {
        String trimmedValue = Objects.requireNonNull(value, fieldName).trim();
        if (trimmedValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        if (!isLegacyDestinationAllowed && exceedsDestinationLimit(trimmedValue)) {
            throw new IllegalArgumentException(fieldName + " cannot exceed "
                    + MAX_DESTINATION_LENGTH + " characters.");
        }
        return trimmedValue;
    }

    /**
     * Checks whether a Plan destination exceeds the current length limit.
     *
     * @param value Destination to measure.
     * @return True when the destination exceeds the limit.
     */
    private static boolean exceedsDestinationLimit(String value) {
        return value.codePointCount(0, value.length()) > MAX_DESTINATION_LENGTH;
    }
}
