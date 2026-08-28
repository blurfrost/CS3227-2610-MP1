package doggo.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import doggo.domain.Plan;
import doggo.domain.Trip;
import doggo.domain.TripStatus;

/**
 * Coordinates presentation-independent doggo use cases.
 */
public final class DoggoService {
    private static final Comparator<DashboardEntry> DASHBOARD_ENTRY_ORDER =
            Comparator.comparing((DashboardEntry entry) -> entry.plan().time())
                    .thenComparing(DashboardEntry::tripTitle)
                    .thenComparing(entry -> entry.plan().destination())
                    .thenComparing(DashboardEntry::tripId)
                    .thenComparing(entry -> entry.plan().id());

    private final TripRepository tripRepository;
    private final Clock clock;

    /**
     * Creates a service backed by the specified Trip repository and Clock.
     *
     * @param tripRepository Repository used by the service.
     * @param clock Clock used for date-sensitive behavior.
     */
    public DoggoService(TripRepository tripRepository, Clock clock) {
        this.tripRepository = Objects.requireNonNull(tripRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Creates and stores a Trip.
     *
     * @param title Trip title.
     * @param startDate Trip start date.
     * @param endDate Trip end date.
     * @return Created Trip.
     */
    public Trip createTrip(String title, LocalDate startDate, LocalDate endDate) {
        Trip trip = new Trip(UUID.randomUUID(), title, startDate, endDate);
        tripRepository.save(trip);
        return trip;
    }

    /**
     * Returns all Trips in deterministic start-date order.
     *
     * @return Sorted Trips.
     */
    public List<Trip> getTrips() {
        return tripRepository.findAll().stream()
                .sorted(Comparator.comparing(Trip::startDate)
                        .thenComparing(Trip::title)
                        .thenComparing(Trip::id))
                .toList();
    }

    /**
     * Returns the current status of the specified Trip.
     *
     * @param trip Trip to classify.
     * @return Status of the Trip relative to the current date.
     * @throws NullPointerException If trip is null.
     */
    public TripStatus getTripStatus(Trip trip) {
        Objects.requireNonNull(trip);
        return trip.statusOn(LocalDate.now(clock));
    }

    /**
     * Returns all Plans scheduled for the current date with their owning Trip context.
     *
     * @return Dashboard entries in deterministic chronological order.
     */
    public List<DashboardEntry> getDashboardEntries() {
        LocalDate currentDate = LocalDate.now(clock);
        return tripRepository.findAll().stream()
                .flatMap(trip -> trip.plans().stream()
                        .filter(plan -> plan.date().equals(currentDate))
                        .map(plan -> new DashboardEntry(trip.id(), trip.title(), plan)))
                .sorted(DASHBOARD_ENTRY_ORDER)
                .toList();
    }

    /**
     * Returns all current and future Trips in deterministic start-date order.
     *
     * @return Current and future Trips.
     */
    public List<Trip> getCurrentAndFutureTrips() {
        LocalDate currentDate = LocalDate.now(clock);
        return getTrips().stream()
                .filter(trip -> trip.statusOn(currentDate) != TripStatus.PAST)
                .toList();
    }

    /**
     * Returns all past Trips in deterministic start-date order.
     *
     * @return Past Trips.
     */
    public List<Trip> getPastTrips() {
        LocalDate currentDate = LocalDate.now(clock);
        return getTrips().stream()
                .filter(trip -> trip.statusOn(currentDate) == TripStatus.PAST)
                .toList();
    }

    /**
     * Returns the Trip with the specified identity.
     *
     * @param id Trip identity.
     * @return Matching Trip, if one exists.
     */
    public Optional<Trip> getTrip(UUID id) {
        return tripRepository.findById(id);
    }

    /**
     * Returns the specified Trip's Plans in deterministic chronological order.
     *
     * @param trip Trip whose Plans are returned.
     * @return Sorted Plans.
     */
    public List<Plan> getPlans(Trip trip) {
        Objects.requireNonNull(trip);
        return trip.plans().stream()
                .sorted(Comparator.comparing(Plan::date)
                        .thenComparing(Plan::time)
                        .thenComparing(Plan::id))
                .toList();
    }

    /**
     * Adds and stores a Plan in the specified Trip.
     *
     * @param tripId Selected Trip identity.
     * @param destination Plan destination.
     * @param date Plan date.
     * @param time Plan time.
     * @return Created Plan.
     */
    public Plan addPlan(UUID tripId, String destination, LocalDate date, LocalTime time) {
        Trip trip = getTrip(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found."));
        Plan plan = new Plan(UUID.randomUUID(), destination, date, time);
        Trip updatedTrip = trip.withAddedPlan(plan);
        tripRepository.save(updatedTrip);
        return plan;
    }

    /**
     * Deletes the Trip aggregate with the specified identity.
     *
     * @param tripId Trip identity.
     * @throws IllegalArgumentException If the Trip does not exist.
     */
    public void deleteTrip(UUID tripId) {
        getTrip(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found."));
        tripRepository.delete(tripId);
    }

    /**
     * Deletes one Plan from the specified Trip aggregate.
     *
     * @param tripId Trip identity.
     * @param planId Plan identity.
     * @throws IllegalArgumentException If the Trip or Plan does not exist.
     */
    public void deletePlan(UUID tripId, UUID planId) {
        Trip trip = getTrip(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found."));
        Trip updatedTrip = trip.withRemovedPlan(planId);
        tripRepository.save(updatedTrip);
    }

    /**
     * Updates and stores a Trip with the specified identity.
     *
     * @param tripId Trip identity.
     * @param title Updated Trip title.
     * @param startDate Updated inclusive start date.
     * @param endDate Updated inclusive end date.
     * @return Updated Trip.
     * @throws IllegalArgumentException If the Trip or new details are invalid.
     */
    public Trip editTrip(UUID tripId, String title, LocalDate startDate, LocalDate endDate) {
        Trip trip = getTrip(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found."));
        Trip updatedTrip = trip.withUpdatedDetails(title, startDate, endDate);
        tripRepository.save(updatedTrip);
        return updatedTrip;
    }

    /**
     * Updates and stores a Plan in the specified Trip.
     *
     * @param tripId Trip identity.
     * @param planId Plan identity.
     * @param destination Updated Plan destination.
     * @param date Updated Plan date.
     * @param time Updated Plan time.
     * @return Updated Plan.
     * @throws IllegalArgumentException If the Trip, Plan, or new details are invalid.
     */
    public Plan editPlan(UUID tripId, UUID planId, String destination, LocalDate date, LocalTime time) {
        Trip trip = getTrip(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found."));
        trip.plans().stream()
                .filter(plan -> plan.id().equals(planId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Plan not found."));
        Plan updatedPlan = new Plan(planId, destination, date, time);
        Trip updatedTrip = trip.withReplacedPlan(updatedPlan);
        tripRepository.save(updatedTrip);
        return updatedPlan;
    }
}
