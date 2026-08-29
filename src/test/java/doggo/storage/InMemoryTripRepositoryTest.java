package doggo.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class InMemoryTripRepositoryTest {
    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        InMemoryTripRepository repository = new InMemoryTripRepository();

        assertEquals(List.of(), repository.findAll());
    }

    @Test
    void save_trip_findByIdReturnsTrip() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        Trip trip = createTrip("Japan");

        repository.save(trip);

        Trip storedTrip = repository.findById(trip.id()).orElseThrow();
        assertEquals(trip.id(), storedTrip.id());
        assertEquals(trip.title(), storedTrip.title());
    }

    @Test
    void findById_missingId_returnsEmptyOptional() {
        InMemoryTripRepository repository = new InMemoryTripRepository();

        assertEquals(Optional.empty(), repository.findById(UUID.randomUUID()));
    }

    @Test
    void save_existingId_replacesStoredTrip() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        UUID tripId = UUID.randomUUID();
        Trip firstTrip = createTrip(tripId, "Japan");
        Trip replacement = createTrip(tripId, "Korea");

        repository.save(firstTrip);
        repository.save(replacement);

        Trip storedTrip = repository.findById(tripId).orElseThrow();
        assertEquals("Korea", storedTrip.title());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findAll_savedTrips_returnsEveryStoredTrip() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        Trip firstTrip = createTrip("Japan");
        Trip secondTrip = createTrip("Korea");

        repository.save(firstTrip);
        repository.save(secondTrip);

        Set<UUID> storedTripIds = repository.findAll().stream()
                .map(Trip::id)
                .collect(Collectors.toSet());

        assertEquals(Set.of(firstTrip.id(), secondTrip.id()), storedTripIds);
    }

    @Test
    void save_aggregateWithPlansAndReviews_preservesAggregate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        Review tripReview = new Review(OptionalInt.of(5), Optional.of("Great trip"));
        Review planReview = new Review(OptionalInt.empty(), Optional.of("Worth revisiting"));
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0)).withReview(planReview);
        Trip trip = createTrip("Japan").withAddedPlan(plan).withReview(tripReview);

        repository.save(trip);

        Trip storedTrip = repository.findById(trip.id()).orElseThrow();
        assertEquals(trip.id(), storedTrip.id());
        assertEquals(trip.title(), storedTrip.title());
        assertEquals(trip.startDate(), storedTrip.startDate());
        assertEquals(trip.endDate(), storedTrip.endDate());
        assertEquals(Optional.of(tripReview), storedTrip.review());
        Plan storedPlan = storedTrip.plans().get(0);
        assertEquals(plan.id(), storedPlan.id());
        assertEquals(plan.destination(), storedPlan.destination());
        assertEquals(plan.date(), storedPlan.date());
        assertEquals(plan.time(), storedPlan.time());
        assertEquals(Optional.of(planReview), storedPlan.review());
    }

    @Test
    void delete_existingTrip_removesTrip() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        Trip trip = createTrip("Japan");
        repository.save(trip);

        repository.delete(trip.id());

        assertTrue(repository.findById(trip.id()).isEmpty());
        assertEquals(List.of(), repository.findAll());
    }

    @Test
    void delete_missingTrip_leavesRepositoryUnchanged() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        Trip trip = createTrip("Japan");
        repository.save(trip);

        repository.delete(UUID.randomUUID());

        assertEquals(List.of(trip), repository.findAll());
    }

    private static Trip createTrip(String title) {
        return createTrip(UUID.randomUUID(), title);
    }

    private static Trip createTrip(UUID id, String title) {
        return new Trip(id, title, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
    }
}
