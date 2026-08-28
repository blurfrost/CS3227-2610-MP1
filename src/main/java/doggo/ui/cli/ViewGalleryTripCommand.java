package doggo.ui.cli;

import java.util.Optional;
import java.util.UUID;

import doggo.domain.Trip;

/**
 * Opens one past Trip from the displayed Gallery list.
 */
final class ViewGalleryTripCommand implements Command {
    private final int index;

    ViewGalleryTripCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        Optional<UUID> tripId = context.session().tripIdAt(index);
        if (tripId.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    context.formatter().invalidIndex("view", IndexedEntity.TRIP,
                            context.session().displayedTripCount()) + "\n"
                            + context.galleryMenu()), false);
        }

        Optional<Trip> trip = context.service().getPastTrips().stream()
                .filter(candidate -> candidate.id().equals(tripId.orElseThrow()))
                .findFirst();
        if (trip.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "The selected Trip is no longer available in Gallery.\n"
                            + context.galleryMenu()), false);
        }

        Trip selectedTrip = trip.orElseThrow();
        context.session().enterGalleryTrip(selectedTrip.id());
        return new CommandResult(context.selectedGalleryTripView(selectedTrip), false);
    }
}
