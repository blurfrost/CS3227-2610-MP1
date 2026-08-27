package doggo.storage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import doggo.application.TripRepository;
import doggo.domain.Trip;

/**
 * Stores Trip aggregates in memory for the first CLI implementation.
 */
public final class InMemoryTripRepository implements TripRepository {
    private final Map<UUID, Trip> trips = new LinkedHashMap<>();

    @Override
    public List<Trip> findAll() {
        return List.copyOf(trips.values());
    }

    @Override
    public Optional<Trip> findById(UUID id) {
        return Optional.ofNullable(trips.get(id));
    }

    @Override
    public void save(Trip trip) {
        trips.put(trip.id(), trip);
    }

    @Override
    public void delete(UUID id) {
        trips.remove(id);
    }

}
