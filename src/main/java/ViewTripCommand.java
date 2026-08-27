import java.util.Optional;
import java.util.UUID;

final class ViewTripCommand implements Command {
    private final int index;

    ViewTripCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        Optional<UUID> tripId = context.session().tripIdAt(index);
        if (tripId.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "Trip index must refer to a listed Trip.\n"
                            + context.organiseMenu()), false);
        }
        Optional<Trip> trip = context.service().getTrip(tripId.orElseThrow());
        if (trip.isEmpty()) {
            return new CommandResult(context.formatter().error(
                    "The selected Trip is no longer available.\n"
                            + context.organiseMenu()), false);
        }
        Trip selectedTrip = trip.orElseThrow();
        context.session().enterTrip(selectedTrip.id());
        return new CommandResult(context.formatter().tripView(
                selectedTrip, context.service().getPlans(selectedTrip)), false);
    }
}
