package doggo.ui.cli;

import java.util.Optional;
import java.util.UUID;

import doggo.domain.Review;
import doggo.domain.Trip;

/**
 * Adds, updates, or removes a Review on a Trip displayed in Gallery.
 */
final class ReviewTripCommand implements Command {
    private final int index;

    ReviewTripCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        Optional<UUID> tripId = context.session().tripIdAt(index);
        if (tripId.isEmpty()) {
            return galleryResult(context, context.formatter().error(
                    context.formatter().invalidIndex("review", IndexedEntity.TRIP,
                            context.session().displayedTripCount())));
        }

        Optional<Trip> trip = findGalleryTrip(context, tripId.orElseThrow());
        if (trip.isEmpty()) {
            return galleryResult(context, context.formatter().error(
                    "The selected Trip is no longer available in Gallery."));
        }

        ReviewInputHelper.Result input = ReviewInputHelper.prompt(context,
                trip.orElseThrow().review());
        if (input.endOfInput()) {
            return new CommandResult("Bye!", true);
        }

        Optional<Trip> refreshedTrip = findGalleryTrip(context, tripId.orElseThrow());
        if (refreshedTrip.isEmpty()) {
            return galleryResult(context, context.formatter().error(
                    "The selected Trip is no longer available in Gallery."));
        }

        Trip currentTrip = refreshedTrip.orElseThrow();
        Optional<Review> existingReview = currentTrip.review();
        Optional<Review> submittedReview = input.review();
        if (existingReview.equals(submittedReview)) {
            return galleryResult(context, "No changes made.");
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
            return galleryResult(context, context.formatter().error(exception.getMessage()));
        }
        return galleryResult(context, resultMessage);
    }

    /**
     * Finds a Trip that is still present in the current Gallery membership.
     *
     * @param context CLI dependencies.
     * @param tripId Retained Trip identity.
     * @return Matching past Trip, if one exists.
     */
    private static Optional<Trip> findGalleryTrip(CliContext context, UUID tripId) {
        return context.service().getPastTrips().stream()
                .filter(trip -> trip.id().equals(tripId))
                .findFirst();
    }

    /**
     * Refreshes the Gallery after a review operation.
     *
     * @param context CLI dependencies.
     * @param message Operation result message.
     * @return Result containing the message and refreshed Gallery.
     */
    private static CommandResult galleryResult(CliContext context, String message) {
        return new CommandResult(message + "\n" + context.galleryMenu(), false);
    }
}
