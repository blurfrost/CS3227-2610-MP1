import java.util.List;

final class SelectTripCommand implements Command {
    private final int index;

    SelectTripCommand(int index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(CliContext context) {
        List<Trip> trips = context.service().getTrips();
        if (index < 1 || index > trips.size()) {
            return new CommandResult(context.formatter().error(
                    "Trip index must refer to a listed Trip.\n"
                            + context.formatter().organiseMenu(trips)), false);
        }
        Trip trip = trips.get(index - 1);
        context.session().setSelectedTripId(trip.id());
        context.session().setMode(CliMode.TRIP);
        return new CommandResult(context.formatter().tripView(
                trip, context.service().getPlans(trip)), false);
    }
}
