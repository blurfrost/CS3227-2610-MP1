package doggo.ui.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import doggo.domain.Review;

/**
 * Formats Reviews consistently across JavaFX detail panes.
 */
final class ReviewDisplaySupport {
    private ReviewDisplaySupport() {
    }

    /**
     * Returns display text for an optional Review.
     *
     * @param review Review to display, or empty when none is recorded.
     * @return Human-readable Review text.
     */
    static String format(Optional<Review> review) {
        Objects.requireNonNull(review);
        if (review.isEmpty()) {
            return "No review recorded yet.";
        }

        Review value = review.orElseThrow();
        List<String> fields = new ArrayList<>(2);
        value.rating().ifPresent(rating -> fields.add("Rating: " + rating + "/5"));
        value.text().ifPresent(notes -> fields.add("Notes: " + notes));
        return String.join("\n", fields);
    }
}
