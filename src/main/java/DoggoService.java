import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Coordinates presentation-independent doggo use cases.
 */
final class DoggoService {
    private final TripRepository tripRepository;

    /**
     * Creates a service backed by the specified Trip repository.
     *
     * @param tripRepository Repository used by the service.
     */
    DoggoService(TripRepository tripRepository) {
        this.tripRepository = Objects.requireNonNull(tripRepository);
    }

    /**
     * Creates and stores a Trip.
     *
     * @param title Trip title.
     * @param startDate Trip start date.
     * @param endDate Trip end date.
     * @return Created Trip.
     */
    Trip createTrip(String title, LocalDate startDate, LocalDate endDate) {
        Trip trip = new Trip(UUID.randomUUID(), title, startDate, endDate);
        tripRepository.save(trip);
        return trip;
    }

    /**
     * Returns all Trips in deterministic start-date order.
     *
     * @return Sorted Trips.
     */
    List<Trip> getTrips() {
        return tripRepository.findAll().stream()
                .sorted(Comparator.comparing(Trip::startDate)
                        .thenComparing(Trip::title)
                        .thenComparing(Trip::id))
                .toList();
    }

    /**
     * Returns the Trip with the specified identity.
     *
     * @param id Trip identity.
     * @return Matching Trip, if one exists.
     */
    Optional<Trip> getTrip(UUID id) {
        return tripRepository.findById(id);
    }

    /**
     * Returns the specified Trip's Plans in deterministic chronological order.
     *
     * @param trip Trip whose Plans are returned.
     * @return Sorted Plans.
     */
    List<Plan> getPlans(Trip trip) {
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
    Plan addPlan(UUID tripId, String destination, LocalDate date, LocalTime time) {
        Trip trip = getTrip(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found."));
        Plan plan = new Plan(UUID.randomUUID(), destination, date, time);
        trip.addPlan(plan);
        tripRepository.save(trip);
        return plan;
    }
}
