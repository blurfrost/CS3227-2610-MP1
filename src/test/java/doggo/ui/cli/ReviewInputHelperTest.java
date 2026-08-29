package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.OptionalInt;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.domain.Review;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class ReviewInputHelperTest {
    @Test
    void prompt_newReview_returnsNormalizedRatingAndText() {
        ReviewInputHelper.Result result = prompt("5\n  Excellent trip.  \n", Optional.empty());

        assertEquals(Optional.of(new Review(OptionalInt.of(5), Optional.of("Excellent trip."))),
                result.review());
        assertFalse(result.endOfInput());
    }

    @Test
    void prompt_blankFields_preservesExistingReview() {
        Review existingReview = new Review(OptionalInt.of(4), Optional.of("Great trip"));

        ReviewInputHelper.Result result = prompt("\n\n", Optional.of(existingReview));

        assertEquals(Optional.of(existingReview), result.review());
    }

    @Test
    void prompt_clearTokens_removesSelectedFields() {
        Review existingReview = new Review(OptionalInt.of(4), Optional.of("Great trip"));

        ReviewInputHelper.Result clearRating = prompt("-\n\n", Optional.of(existingReview));
        ReviewInputHelper.Result clearText = prompt("\n-\n", Optional.of(existingReview));
        ReviewInputHelper.Result clearReview = prompt("-\n-\n", Optional.of(existingReview));

        assertEquals(Optional.of(new Review(OptionalInt.empty(), Optional.of("Great trip"))),
                clearRating.review());
        assertEquals(Optional.of(new Review(OptionalInt.of(4), Optional.empty())),
                clearText.review());
        assertEquals(Optional.empty(), clearReview.review());
    }

    @Test
    void prompt_invalidRating_repromptsUntilValidInput() {
        StringWriter output = new StringWriter();

        ReviewInputHelper.Result result = prompt("0\n3\nGood trip\n", Optional.empty(), output);

        assertEquals(Optional.of(new Review(OptionalInt.of(3), Optional.of("Good trip"))),
                result.review());
        assertTrue(output.toString().contains("Rating must be a whole number from 1 to 5."));
    }

    @Test
    void prompt_endOfInput_returnsEndOfInputResult() {
        ReviewInputHelper.Result result = prompt("", Optional.empty());

        assertTrue(result.endOfInput());
        assertEquals(Optional.empty(), result.review());
    }

    private static ReviewInputHelper.Result prompt(String input, Optional<Review> existingReview) {
        return prompt(input, existingReview, new StringWriter());
    }

    private static ReviewInputHelper.Result prompt(String input, Optional<Review> existingReview,
                                                   StringWriter output) {
        PrintWriter printWriter = new PrintWriter(output);
        CliContext context = new CliContext(
                new DoggoService(new InMemoryTripRepository(), TestClock.fixed()),
                new CliSession(),
                new CliPrompter(new BufferedReader(new StringReader(input)), printWriter),
                new CliFormatter(),
                printWriter);

        return ReviewInputHelper.prompt(context, existingReview);
    }
}
