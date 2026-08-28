package doggo.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import doggo.TestClock;
import doggo.domain.Plan;
import doggo.domain.Trip;
import doggo.domain.TripStatus;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class DoggoServiceTest {
    @Test
    void createService_nullClock_throwsException() {
        assertThrows(NullPointerException.class,
                () -> new DoggoService(new InMemoryTripRepository(), null));
    }

    @Test
    void createTrip_andGetTrips_returnsTripsInStartDateOrder() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip future = service.createTrip("Future", LocalDate.of(2028, 1, 1),
                LocalDate.of(2028, 1, 2));
        Trip past = service.createTrip("Past", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2));
        Trip current = service.createTrip("Current", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));

        assertEquals(List.of(past, current, future), service.getTrips());
    }

    @Test
    void getCurrentAndFutureTrips_excludesPastTrips() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        service.createTrip("Past", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 4));
        Trip current = service.createTrip("Current", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));
        Trip future = service.createTrip("Future", LocalDate.of(2027, 1, 6),
                LocalDate.of(2027, 1, 9));

        assertEquals(List.of(current, future), service.getCurrentAndFutureTrips());
    }

    @Test
    void getPastTrips_excludesCurrentAndFutureTrips() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip past = service.createTrip("Past", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 4));
        service.createTrip("Current", LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));
        service.createTrip("Future", LocalDate.of(2027, 1, 6), LocalDate.of(2027, 1, 9));

        assertEquals(List.of(past), service.getPastTrips());
    }

    @Test
    void statusQueries_includeTripsOnInclusiveDateBoundaries() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip endingToday = service.createTrip("Ending today", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 5));
        Trip startingToday = service.createTrip("Starting today", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 9));
        Trip singleDay = service.createTrip("Single day", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));

        assertEquals(List.of(endingToday, singleDay, startingToday),
                service.getCurrentAndFutureTrips());
        assertEquals(List.of(), service.getPastTrips());
    }

    @Test
    void statusQueries_preserveDeterministicOrdering() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip laterTitle = service.createTrip("Zoo", LocalDate.of(2027, 1, 6),
                LocalDate.of(2027, 1, 7));
        Trip earlierTitle = service.createTrip("Aquarium", LocalDate.of(2027, 1, 6),
                LocalDate.of(2027, 1, 7));

        assertEquals(List.of(earlierTitle, laterTitle), service.getCurrentAndFutureTrips());
        assertEquals(TripStatus.FUTURE, earlierTitle.statusOn(LocalDate.of(2027, 1, 5)));
    }

    @Test
    void dashboardEntries_emptyTripsAndTripsWithoutPlans_returnEmptyList() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        repository.save(trip);

        assertEquals(List.of(), service.getDashboardEntries());
    }

    @Test
    void dashboardEntries_filtersPlansByCurrentDateAcrossTrips() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip firstTrip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Trip secondTrip = service.createTrip("Korea", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));
        Plan todayPlan = service.addPlan(firstTrip.id(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        service.addPlan(firstTrip.id(), "Yesterday", LocalDate.of(2027, 1, 4), LocalTime.of(8, 0));
        service.addPlan(firstTrip.id(), "Tomorrow", LocalDate.of(2027, 1, 6), LocalTime.of(8, 0));
        Plan secondTodayPlan = service.addPlan(secondTrip.id(), "Market", LocalDate.of(2027, 1, 5),
                LocalTime.of(10, 0));

        assertEquals(List.of(new DashboardEntry(firstTrip.id(), "Japan", todayPlan),
                new DashboardEntry(secondTrip.id(), "Korea", secondTodayPlan)),
                service.getDashboardEntries());
    }

    @Test
    void dashboardEntries_equalTimes_useDeterministicTieBreakers() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        LocalDate today = LocalDate.of(2027, 1, 5);
        LocalTime sameTime = LocalTime.of(9, 0);
        Trip alphaFirst = new Trip(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Alpha", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        Trip alphaSecond = new Trip(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "Alpha", today, today);
        Trip beta = new Trip(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "Beta", today, today);
        Trip early = new Trip(UUID.fromString("00000000-0000-0000-0000-000000000004"),
                "Zeta", today, today);
        Plan alphaFirstZoo = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000011"),
                "Zoo", today, sameTime);
        Plan alphaFirstZooLaterId = new Plan(
                UUID.fromString("00000000-0000-0000-0000-000000000012"), "Zoo", today, sameTime);
        Plan alphaFirstMuseum = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000013"),
                "Museum", today, sameTime);
        Plan alphaSecondMuseum = new Plan(
                UUID.fromString("00000000-0000-0000-0000-000000000021"), "Museum", today, sameTime);
        Plan betaAirport = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000031"),
                "Airport", today, sameTime);
        Plan earlyPlan = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000041"),
                "Park", today, LocalTime.of(8, 0));
        repository.save(alphaFirst.withAddedPlan(alphaFirstZoo)
                .withAddedPlan(alphaFirstZooLaterId).withAddedPlan(alphaFirstMuseum));
        repository.save(alphaSecond.withAddedPlan(alphaSecondMuseum));
        repository.save(beta.withAddedPlan(betaAirport));
        repository.save(early.withAddedPlan(earlyPlan));

        assertEquals(List.of(new DashboardEntry(early.id(), "Zeta", earlyPlan),
                new DashboardEntry(alphaFirst.id(), "Alpha", alphaFirstMuseum),
                new DashboardEntry(alphaSecond.id(), "Alpha", alphaSecondMuseum),
                new DashboardEntry(alphaFirst.id(), "Alpha", alphaFirstZoo),
                new DashboardEntry(alphaFirst.id(), "Alpha", alphaFirstZooLaterId),
                new DashboardEntry(beta.id(), "Beta", betaAirport)),
                service.getDashboardEntries());
    }

    @Test
    void addPlan_toExistingTrip_storesPlan() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));

        service.addPlan(trip.id(), "Mount Fuji", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));

        assertEquals(1, service.getTrip(trip.id()).orElseThrow().plans().size());
    }

    @Test
    void getPlans_unsortedPlans_returnsChronologicalOrder() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        Plan laterDate = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "Osaka", LocalDate.of(2027, 1, 6), LocalTime.of(9, 0));
        Plan laterTime = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.of(10, 0));
        Plan earlierId = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Mount Fuji", LocalDate.of(2027, 1, 5), LocalTime.of(10, 0));
        trip = trip.withAddedPlan(laterDate);
        trip = trip.withAddedPlan(laterTime);
        trip = trip.withAddedPlan(earlierId);

        assertEquals(List.of(earlierId, laterTime, laterDate), service.getPlans(trip));
    }

    @Test
    void addPlan_failedSave_preservesPreviouslyStoredTrip() {
        FailingTripRepository repository = new FailingTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        repository.setShouldFail(true);

        assertThrows(RepositoryException.class, () -> service.addPlan(trip.id(), "Mount Fuji",
                LocalDate.of(2027, 1, 5), LocalTime.of(9, 0)));
        assertEquals(0, service.getTrip(trip.id()).orElseThrow().plans().size());
    }

    @Test
    void deleteTrip_removesTripAndAllPlans() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        service.addPlan(trip.id(), "Mount Fuji", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));

        service.deleteTrip(trip.id());

        assertEquals(Optional.empty(), service.getTrip(trip.id()));
        assertEquals(0, service.getTrips().size());
    }

    @Test
    void deletePlan_removesOnlySelectedPlan() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan firstPlan = service.addPlan(trip.id(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Plan secondPlan = service.addPlan(trip.id(), "Osaka", LocalDate.of(2027, 1, 6),
                LocalTime.of(9, 0));

        service.deletePlan(trip.id(), firstPlan.id());

        assertEquals(List.of(secondPlan), service.getTrip(trip.id()).orElseThrow().plans());
    }

    @Test
    void deleteTrip_missingTrip_rejectsDeletion() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());

        assertThrows(IllegalArgumentException.class, () -> service.deleteTrip(UUID.randomUUID()));
    }

    @Test
    void deletePlan_missingTrip_rejectsDeletion() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());

        assertThrows(IllegalArgumentException.class, () -> service.deletePlan(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void deletePlan_missingPlan_rejectsDeletion() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertThrows(IllegalArgumentException.class,
                () -> service.deletePlan(trip.id(), UUID.randomUUID()));
    }

    @Test
    void editTrip_updatesDetailsAndPreservesIdentity() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        Trip updatedTrip = service.editTrip(trip.id(), " Korea ", LocalDate.of(2027, 1, 2),
                LocalDate.of(2027, 1, 10));

        assertEquals(trip.id(), updatedTrip.id());
        assertEquals("Korea", updatedTrip.title());
        assertEquals(updatedTrip, service.getTrip(trip.id()).orElseThrow());
    }

    @Test
    void editPlan_updatesOnlySelectedPlanAndPreservesIdentities() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan originalPlan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Plan otherPlan = service.addPlan(trip.id(), "Osaka", LocalDate.of(2027, 1, 6),
                LocalTime.of(9, 0));

        Plan updatedPlan = service.editPlan(trip.id(), originalPlan.id(), "Kyoto",
                LocalDate.of(2027, 1, 7), LocalTime.of(10, 0));

        assertEquals(originalPlan.id(), updatedPlan.id());
        assertEquals(List.of(otherPlan, updatedPlan), service.getPlans(
                service.getTrip(trip.id()).orElseThrow()));
    }

    @Test
    void editTrip_excludingPlan_rejectsUpdateAndPreservesStoredTrip() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));

        assertThrows(IllegalArgumentException.class, () -> service.editTrip(trip.id(), "Korea",
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 4)));
        assertEquals(LocalDate.of(2027, 1, 9), service.getTrip(trip.id()).orElseThrow().endDate());
    }

    @Test
    void editPlan_missingPlan_rejectsUpdate() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertThrows(IllegalArgumentException.class, () -> service.editPlan(trip.id(),
                UUID.randomUUID(), "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0)));
    }

    @Test
    void editTrip_missingTrip_rejectsUpdate() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());

        assertThrows(IllegalArgumentException.class, () -> service.editTrip(UUID.randomUUID(),
                "Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9)));
    }

    @Test
    void editPlan_outsideTripDates_rejectsUpdate() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        assertThrows(IllegalArgumentException.class, () -> service.editPlan(trip.id(), plan.id(),
                "Kyoto", LocalDate.of(2027, 1, 10), LocalTime.of(10, 0)));
    }

    @Test
    void editTrip_failedSave_preservesPreviouslyStoredTrip() {
        FailingTripRepository repository = new FailingTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        repository.setShouldFail(true);

        assertThrows(RepositoryException.class, () -> service.editTrip(trip.id(), "Korea",
                LocalDate.of(2027, 1, 2), LocalDate.of(2027, 1, 10)));
        assertEquals("Japan", service.getTrip(trip.id()).orElseThrow().title());
    }

    @Test
    void editPlan_failedSave_preservesPreviouslyStoredTrip() {
        FailingTripRepository repository = new FailingTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        repository.setShouldFail(true);

        assertThrows(RepositoryException.class, () -> service.editPlan(trip.id(), plan.id(),
                "Kyoto", LocalDate.of(2027, 1, 6), LocalTime.of(10, 0)));
        assertEquals("Tokyo", service.getTrip(trip.id()).orElseThrow().plans().get(0).destination());
    }

    @Test
    void deleteTrip_failedDelete_preservesStoredTrip() {
        FailingTripRepository repository = new FailingTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        repository.setShouldFail(true);

        assertThrows(RepositoryException.class, () -> service.deleteTrip(trip.id()));
        assertEquals(trip, service.getTrip(trip.id()).orElseThrow());
    }

    private static final class FailingTripRepository implements TripRepository {
        private final InMemoryTripRepository delegate = new InMemoryTripRepository();
        private boolean shouldFail;

        @Override
        public List<Trip> findAll() {
            return delegate.findAll();
        }

        @Override
        public Optional<Trip> findById(UUID id) {
            return delegate.findById(id);
        }

        @Override
        public void save(Trip trip) {
            if (shouldFail) {
                throw new RepositoryException("Simulated repository failure.");
            }
            delegate.save(trip);
        }

        @Override
        public void delete(UUID id) {
            if (shouldFail) {
                throw new RepositoryException("Simulated repository failure.");
            }
            delegate.delete(id);
        }

        void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }
    }
}
