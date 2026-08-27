package doggo.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import doggo.domain.Plan;
import doggo.domain.Trip;

/**
 * Coordinates presentation-independent doggo use cases.
 */
public final class DoggoService {
    private final TripRepository tripRepository;

    /**
     * Creates a service backed by the specified Trip repository.
     *
     * @param tripRepository Repository used by the service.
     */
    public DoggoService(TripRepository tripRepository) {
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
