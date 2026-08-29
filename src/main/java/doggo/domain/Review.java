package doggo.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represents an immutable review for a completed Trip or Plan.
 *
 * @param rating Optional whole-number rating from 1 to 5.
 * @param text Optional written review text.
 */
public record Review(OptionalInt rating, Optional<String> text) {
    /**
     * Creates a review with an optional rating and optional written text.
     *
     * @param rating Optional whole-number rating from 1 to 5.
     * @param text Optional written review text.
     * @throws NullPointerException If either optional argument is null.
     * @throws IllegalArgumentException If the rating is outside 1 to 5, or both fields are absent.
     */
    public Review {
        Objects.requireNonNull(rating, "Rating cannot be null.");
        Objects.requireNonNull(text, "Review text cannot be null.");
        if (rating.isPresent() && (rating.getAsInt() < 1 || rating.getAsInt() > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        text = text.map(String::trim)
                .filter(value -> !value.isBlank());
        if (rating.isEmpty() && text.isEmpty()) {
            throw new IllegalArgumentException("Review must contain a rating or text.");
        }
    }
}
