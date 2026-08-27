import java.io.PrintWriter;
import java.util.List;

record CliContext(DoggoService service, CliSession session, CliPrompter prompter,
                  CliFormatter formatter, PrintWriter output) {
    String organiseMenu() {
        List<Trip> trips = service.getTrips();
        session.setDisplayedTripIds(trips.stream().map(Trip::id).toList());
        return formatter.organiseMenu(trips);
    }

    String selectedTripView(Trip trip) {
        List<Plan> plans = service.getPlans(trip);
        session.setDisplayedPlanIds(plans.stream().map(Plan::id).toList());
        return formatter.tripView(trip, plans);
    }
}
