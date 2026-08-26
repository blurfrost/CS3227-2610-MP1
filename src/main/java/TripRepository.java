import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines persistence operations for Trip aggregates.
 */
interface TripRepository {
    /**
     * Returns all stored Trip aggregates.
     *
     * @return Stored Trips.
     * @throws RepositoryException If the Trips cannot be read.
     */
    List<Trip> findAll();

    /**
     * Returns the Trip with the specified identity.
     *
     * @param id Trip identity.
     * @return Matching Trip, if one exists.
     * @throws RepositoryException If the Trip cannot be read.
     */
    Optional<Trip> findById(UUID id);

    /**
     * Stores the specified Trip aggregate as one repository update.
     *
     * @param trip Trip aggregate to store.
     * @throws RepositoryException If the Trip cannot be stored.
     */
    void save(Trip trip);
}
