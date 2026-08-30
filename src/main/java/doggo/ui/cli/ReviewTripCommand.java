package doggo.ui.cli;

import java.util.Optional;
import java.util.UUID;

import doggo.domain.Review;
import doggo.domain.Trip;

/**
 * Adds, updates, or removes a Review on a Trip displayed in a Trip list.
 */
final class ReviewTripCommand implements Command {
    private final int index;

    ReviewTripCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        CliMode initiatingMode = context.session().mode();
        Optional<UUID> tripId = context.session().tripIdAt(index);
        if (tripId.isEmpty()) {
            String invalidIndexMessage = context.formatter().invalidIndex("review", IndexedEntity.TRIP,
                    context.session().displayedTripCount());
            return tripResult(context, context.formatter().error(invalidIndexMessage), initiatingMode);
        }

        Optional<Trip> trip = context.displayedTripAt(index, initiatingMode);
        if (trip.isEmpty()) {
            return tripResult(context, context.formatter().error(
                    "The selected Trip is no longer available."), initiatingMode);
        }

        ReviewInputHelper.Result input = ReviewInputHelper.prompt(context,
                trip.orElseThrow().review());
        if (input.endOfInput()) {
            return new CommandResult("Bye!", true);
        }

        Optional<Trip> refreshedTrip = context.displayedTripAt(index, initiatingMode);
        if (refreshedTrip.isEmpty()) {
            return tripResult(context, context.formatter().error(
                    "The selected Trip is no longer available."), initiatingMode);
        }

        Trip currentTrip = refreshedTrip.orElseThrow();
        Optional<Review> existingReview = currentTrip.review();
        Optional<Review> submittedReview = input.review();
        if (existingReview.equals(submittedReview)) {
            return tripResult(context, "No changes made.", initiatingMode);
        }

        String resultMessage;
        try {
            if (submittedReview.isEmpty()) {
                context.service().removeTripReview(tripId.orElseThrow());
                resultMessage = "Review removed.";
            } else {
                context.service().setTripReview(tripId.orElseThrow(), submittedReview.orElseThrow());
                resultMessage = existingReview.isEmpty() ? "Review added." : "Review updated.";
            }
        } catch (IllegalArgumentException exception) {
            return tripResult(context, context.formatter().error(exception.getMessage()), initiatingMode);
        }
        return tripResult(context, resultMessage, initiatingMode);
    }

    /**
     * Returns a Trip review result with the initiating Trip list refreshed.
     *
     * @param context CLI dependencies.
     * @param message Operation result message.
     * @param initiatingMode Trip list mode from which the command started.
     * @return Result containing the message and refreshed Trip list.
     */
    private static CommandResult tripResult(CliContext context, String message,
                                             CliMode initiatingMode) {
        return new CommandResult(message + "\n" + context.refreshTripList(initiatingMode), false);
    }
}
