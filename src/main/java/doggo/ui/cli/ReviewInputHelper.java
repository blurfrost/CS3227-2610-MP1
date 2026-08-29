package doggo.ui.cli;

import java.util.Optional;
import java.util.OptionalInt;

import doggo.domain.Review;

/**
 * Collects and normalizes the two fields used by a Review.
 */
final class ReviewInputHelper {
    private ReviewInputHelper() {
    }

    /**
     * Prompts for a Review while preserving or clearing existing fields as requested.
     *
     * @param context CLI dependencies.
     * @param existingReview Review being replaced, if one exists.
     * @return Prompt result containing the submitted Review, or an end-of-input result.
     */
    static Result prompt(CliContext context, Optional<Review> existingReview) {
        OptionalInt existingRating = existingReview.map(Review::rating).orElse(OptionalInt.empty());
        OptionalInt rating = promptRating(context, existingRating);
        if (rating == null) {
            return Result.eof();
        }

        Optional<String> existingText = existingReview.flatMap(Review::text);
        String textInput = context.prompter().prompt(textPrompt(existingText));
        if (textInput == null) {
            return Result.eof();
        }
        Optional<String> text = parseText(textInput, existingText);
        if (rating.isEmpty() && text.isEmpty()) {
            return Result.completed(Optional.empty());
        }
        return Result.completed(Optional.of(new Review(rating, text)));
    }

    /**
     * Prompts for a rating until the input is blank, a clearing token, or a whole number from 1 to 5.
     *
     * @param context CLI dependencies.
     * @param existingRating Existing rating, if one exists.
     * @return Submitted rating, or null when input ends.
     */
    private static OptionalInt promptRating(CliContext context, OptionalInt existingRating) {
        while (true) {
            String input = context.prompter().prompt(ratingPrompt(existingRating));
            if (input == null) {
                return null;
            }
            String trimmedInput = input.trim();
            if (trimmedInput.isEmpty()) {
                return existingRating;
            }
            if (trimmedInput.equals("-")) {
                return OptionalInt.empty();
            }
            if (!trimmedInput.matches("\\d+")) {
                printInvalidRating(context);
                continue;
            }
            try {
                int rating = Integer.parseInt(trimmedInput);
                if (rating >= 1 && rating <= 5) {
                    return OptionalInt.of(rating);
                }
            } catch (NumberFormatException exception) {
                // The input is too large to parse as an int and is therefore invalid.
            }
            printInvalidRating(context);
        }
    }

    /**
     * Parses one line of review text, preserving an existing value on blank input.
     *
     * @param input User-entered text.
     * @param existingText Existing text, if one exists.
     * @return Parsed text.
     */
    private static Optional<String> parseText(String input, Optional<String> existingText) {
        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            return existingText;
        }
        if (trimmedInput.equals("-")) {
            return Optional.empty();
        }
        return Optional.of(input);
    }

    /**
     * Returns the rating prompt for the current Review state.
     *
     * @param existingRating Existing rating, if one exists.
     * @return Rating prompt.
     */
    private static String ratingPrompt(OptionalInt existingRating) {
        String current = existingRating.isPresent()
                ? "Current: " + existingRating.getAsInt()
                : "No current rating";
        return "Enter rating (1-5), blank to preserve, or - to clear [" + current + "]:";
    }

    /**
     * Returns the text prompt for the current Review state.
     *
     * @param existingText Existing text, if one exists.
     * @return Text prompt.
     */
    private static String textPrompt(Optional<String> existingText) {
        String current = existingText.map(value -> "Current: " + value)
                .orElse("No current review text");
        return "Enter review text, blank to preserve, or - to clear [" + current + "]:";
    }

    /**
     * Prints feedback for a rating that cannot be stored.
     *
     * @param context CLI dependencies.
     */
    private static void printInvalidRating(CliContext context) {
        context.output().println(context.formatter().error("Rating must be a whole number from 1 to 5."));
    }

    /**
     * Represents the outcome of collecting Review input.
     *
     * @param endOfInput Whether the input stream ended during prompting.
     * @param review Submitted Review, or empty when both fields were cleared.
     */
    record Result(boolean endOfInput, Optional<Review> review) {
        Result {
            if (endOfInput && review.isPresent()) {
                throw new IllegalArgumentException("End-of-input results cannot contain a Review.");
            }
        }

        static Result eof() {
            return new Result(true, Optional.empty());
        }

        static Result completed(Optional<Review> review) {
            return new Result(false, review);
        }
    }
}
