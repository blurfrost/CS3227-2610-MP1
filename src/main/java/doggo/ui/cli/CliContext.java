package doggo.ui.cli;

import java.io.PrintWriter;
import java.util.List;

import doggo.application.DashboardEntry;
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
        session.setDisplayedPlanTargets(trip.id(), plans.stream().map(Plan::id).toList());
        return formatter.tripView(trip, plans);
    }

    String dashboardMenu() {
        List<DashboardEntry> entries = service.getDashboardEntries();
        session.setDisplayedPlanTargets(entries.stream()
                .map(entry -> new PlanTarget(entry.tripId(), entry.plan().id()))
                .toList());
        return formatter.dashboardMenu(entries);
    }

    String refreshCurrentView() {
        return switch (session.mode()) {
        case MAIN -> formatter.mainMenu();
        case ORGANISE -> organiseMenu();
        case DASHBOARD -> dashboardMenu();
        case TRIP -> session.selectedTripId()
                .flatMap(service::getTrip)
                .map(this::selectedTripView)
                .orElseGet(() -> {
                    session.enterOrganise();
                    return organiseMenu();
                });
        };
    }
}
