package doggo.ui.cli;

import java.io.PrintWriter;
import java.util.List;

import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Trip;

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
