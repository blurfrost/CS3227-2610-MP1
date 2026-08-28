package doggo.ui.cli;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    Optional<Trip> displayedTripAt(int index, CliMode initiatingMode) {
        Optional<UUID> tripId = session.tripIdAt(index);
        if (tripId.isEmpty()) {
            return Optional.empty();
        }
        return tripsFor(initiatingMode).stream()
                .filter(trip -> trip.id().equals(tripId.orElseThrow()))
                .findFirst();
    }

    String refreshTripList(CliMode mode) {
        return switch (mode) {
        case ORGANISE -> organiseMenu();
        case GALLERY -> galleryMenu();
        default -> throw new IllegalArgumentException("Mode must be a Trip list mode.");
        };
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

    /**
     * Returns Trips belonging to the specified Trip list mode.
     *
     * @param mode Mode whose Trip list is requested.
     * @return Trips visible in the specified mode.
     * @throws IllegalArgumentException If mode is not a Trip list mode.
     */
    private List<Trip> tripsFor(CliMode mode) {
        return switch (mode) {
        case ORGANISE -> service.getCurrentAndFutureTrips();
        case GALLERY -> service.getPastTrips();
        default -> throw new IllegalArgumentException("Mode must be a Trip list mode.");
        };
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
