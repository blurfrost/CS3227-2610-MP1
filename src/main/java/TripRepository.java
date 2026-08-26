import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines persistence operations for Trip aggregates.
 */
interface TripRepository {
    List<Trip> findAll();

    Optional<Trip> findById(UUID id);

    void save(Trip trip);
}
