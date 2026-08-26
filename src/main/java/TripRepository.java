import java.util.List;

/**
 * Defines persistence operations for Trip aggregates.
 */
interface TripRepository {
    List<Trip> findAll();

    void save(Trip trip);
}
