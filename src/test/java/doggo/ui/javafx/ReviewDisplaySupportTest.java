package doggo.ui.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.OptionalInt;

import doggo.domain.Review;

import org.junit.jupiter.api.Test;

class ReviewDisplaySupportTest {
    @Test
    void format_absentReview_returnsEmptyState() {
        assertEquals("No review recorded yet.", ReviewDisplaySupport.format(Optional.empty()));
    }

    @Test
    void format_ratingOnlyReview_returnsOnlyRating() {
        Review review = new Review(OptionalInt.of(4), Optional.empty());

        assertEquals("Rating: 4/5", ReviewDisplaySupport.format(Optional.of(review)));
    }

    @Test
    void format_notesOnlyReview_returnsOnlyNotes() {
        Review review = new Review(OptionalInt.empty(), Optional.of("A memorable journey."));

        assertEquals("Notes: A memorable journey.", ReviewDisplaySupport.format(Optional.of(review)));
    }

    @Test
    void format_combinedReview_returnsRatingAndNotes() {
        Review review = new Review(OptionalInt.of(5), Optional.of("A memorable journey."));

        assertEquals("Rating: 5/5\nNotes: A memorable journey.",
                ReviewDisplaySupport.format(Optional.of(review)));
    }
}
