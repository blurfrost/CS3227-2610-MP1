package doggo.ui.cli;

import java.io.PrintWriter;
import java.util.List;

import doggo.application.DashboardEntry;
import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Trip;
import doggo.domain.TripStatus;

record CliContext(DoggoService service, CliSession session, CliPrompter prompter,
                  CliFormatter formatter, PrintWriter output) {
    String organiseMenu() {
        List<Trip> trips = service.getCurrentAndFutureTrips();
        session.setDisplayedTripIds(trips.stream().map(Trip::id).toList());
        return formatter.organiseMenu(trips);
    }

    String enterTripListFor(Trip trip) {
        if (service.getTripStatus(trip) == TripStatus.PAST) {
            session.enterGallery();
            return galleryMenu();
        }
        session.enterOrganise();
        return organiseMenu();
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

    String galleryMenu() {
        List<Trip> trips = service.getPastTrips();
        session.setDisplayedTripIds(trips.stream().map(Trip::id).toList());
        return formatter.galleryMenu(trips);
    }

    String selectedGalleryTripView(Trip trip) {
        return formatter.galleryTripView(trip, service.getPlans(trip));
    }

    String refreshCurrentView() {
        return switch (session.mode()) {
        case MAIN -> formatter.mainMenu();
        case ORGANISE -> organiseMenu();
        case DASHBOARD -> dashboardMenu();
        case GALLERY -> galleryMenu();
        case TRIP -> session.selectedTripId()
                .flatMap(service::getTrip)
                .map(this::selectedTripView)
                .orElseGet(() -> {
                    session.enterOrganise();
                    return organiseMenu();
                });
        case GALLERY_TRIP -> session.selectedTripId()
                .flatMap(selectedTripId -> service.getPastTrips().stream()
                        .filter(trip -> trip.id().equals(selectedTripId))
                        .findFirst())
                .map(this::selectedGalleryTripView)
                .orElseGet(() -> {
                    session.enterGallery();
                    return galleryMenu();
                });
        };
    }
}
