import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
}
