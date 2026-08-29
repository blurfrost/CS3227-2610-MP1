package doggo.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

class ReviewTest {
    @Test
    void createReview_ratingAndText_storesValuesWithTrimmedText() {
        Review review = new Review(OptionalInt.of(5), Optional.of("  Excellent trip.  "));

        assertAll(
                () -> assertEquals(OptionalInt.of(5), review.rating()),
                () -> assertEquals(Optional.of("Excellent trip."), review.text()));
    }

    @Test
    void createReview_ratingOnly_storesRating() {
        Review review = new Review(OptionalInt.of(3), Optional.empty());

        assertAll(
                () -> assertEquals(OptionalInt.of(3), review.rating()),
                () -> assertEquals(Optional.empty(), review.text()));
    }

    @Test
    void createReview_textOnly_storesText() {
        Review review = new Review(OptionalInt.empty(), Optional.of("  Worth visiting. "));

        assertAll(
                () -> assertEquals(OptionalInt.empty(), review.rating()),
                () -> assertEquals(Optional.of("Worth visiting."), review.text()));
    }

    @Test
    void createReview_blankText_becomesAbsentWhenRatingPresent() {
        Review review = new Review(OptionalInt.of(1), Optional.of("  \t "));

        assertEquals(Optional.empty(), review.text());
    }

    @Test
    void createReview_boundaryRatings_areAccepted() {
        assertAll(
                () -> new Review(OptionalInt.of(1), Optional.empty()),
                () -> new Review(OptionalInt.of(5), Optional.empty()));
    }

    @Test
    void createReview_invalidRating_throwsException() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Review(OptionalInt.of(0), Optional.empty())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new Review(OptionalInt.of(6), Optional.empty())));
    }

    @Test
    void createReview_withoutRatingOrText_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Review(OptionalInt.empty(), Optional.empty()));
        assertThrows(IllegalArgumentException.class,
                () -> new Review(OptionalInt.empty(), Optional.of("  ")));
    }

    @Test
    void createReview_nullArguments_throwsException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new Review(null, Optional.empty())),
                () -> assertThrows(NullPointerException.class,
                        () -> new Review(OptionalInt.empty(), null)));
    }
}
