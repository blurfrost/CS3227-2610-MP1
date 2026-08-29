package doggo.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import doggo.TestClock;
import doggo.domain.Plan;
import doggo.domain.Review;
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
    void getTripStatus_classifiesPastTrip() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Past", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));

        assertEquals(TripStatus.PAST, service.getTripStatus(trip));
    }

    @Test
    void getTripStatus_classifiesTripStartingTodayAsCurrent() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Starting today", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 9));

        assertEquals(TripStatus.CURRENT, service.getTripStatus(trip));
    }

    @Test
    void getTripStatus_classifiesTripEndingTodayAsCurrent() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Ending today", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 5));

        assertEquals(TripStatus.CURRENT, service.getTripStatus(trip));
    }

    @Test
    void getTripStatus_classifiesSingleDayTripAsCurrent() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Single day", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));

        assertEquals(TripStatus.CURRENT, service.getTripStatus(trip));
    }

    @Test
    void getTripStatus_classifiesFutureTrip() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Future", LocalDate.of(2027, 1, 6),
                LocalDate.of(2027, 1, 9));

        assertEquals(TripStatus.FUTURE, service.getTripStatus(trip));
    }

    @Test
    void getTripStatus_nullTrip_throwsException() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());

        assertThrows(NullPointerException.class, () -> service.getTripStatus(null));
    }

    @Test
    void isTripReviewable_onlyPastTripsAreEligible() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip pastTrip = new Trip(UUID.randomUUID(), "Past", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Trip currentTrip = new Trip(UUID.randomUUID(), "Current", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 5));
        Trip futureTrip = new Trip(UUID.randomUUID(), "Future", LocalDate.of(2027, 1, 6),
                LocalDate.of(2027, 1, 9));

        assertTrue(service.isTripReviewable(pastTrip));
        assertFalse(service.isTripReviewable(currentTrip));
        assertFalse(service.isTripReviewable(futureTrip));
    }

    @Test
    void isPlanReviewable_beforeExactAndAfterScheduledTime() {
        Plan plan = new Plan(UUID.randomUUID(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        assertFalse(serviceAt("2027-01-05T08:59:59Z").isPlanReviewable(plan));
        assertTrue(serviceAt("2027-01-05T09:00:00Z").isPlanReviewable(plan));
        assertTrue(serviceAt("2027-01-05T09:00:01Z").isPlanReviewable(plan));
    }

    @Test
    void setReplaceAndRemoveTripReview_returnsAndStoresUpdatedValues() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Review ratingReview = new Review(OptionalInt.of(5), Optional.empty());
        Review textReview = new Review(OptionalInt.empty(), Optional.of(" Great food. "));

        Trip reviewedTrip = service.setTripReview(trip.id(), ratingReview);
        Trip replacedTrip = service.setTripReview(trip.id(), textReview);
        Trip removedTrip = service.removeTripReview(trip.id());

        assertEquals(Optional.of(ratingReview), reviewedTrip.review());
        assertEquals(Optional.of(textReview), replacedTrip.review());
        assertEquals(Optional.empty(), removedTrip.review());
        assertEquals(removedTrip, service.getTrip(trip.id()).orElseThrow());
    }

    @Test
    void setReplaceAndRemovePlanReview_supportsRatingAndTextVariants() {
        DoggoService service = serviceAt("2027-01-05T10:00:00Z");
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Review textReview = new Review(OptionalInt.empty(), Optional.of(" Great food. "));
        Review combinedReview = new Review(OptionalInt.of(4), Optional.of("Worth revisiting."));

        Plan reviewedPlan = service.setPlanReview(trip.id(), plan.id(), textReview);
        Plan replacedPlan = service.setPlanReview(trip.id(), plan.id(), combinedReview);
        Plan removedPlan = service.removePlanReview(trip.id(), plan.id());

        assertEquals(Optional.of(textReview), reviewedPlan.review());
        assertEquals(Optional.of(combinedReview), replacedPlan.review());
        assertEquals(Optional.empty(), removedPlan.review());
        assertEquals(removedPlan, service.getTrip(trip.id()).orElseThrow().plans().get(0));
    }

    @Test
    void setTripReview_ineligibleTrip_rejectsWithActionableMessage() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 5));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.setTripReview(trip.id(), ratingReview(4)));

        assertEquals("Trip can be reviewed only after its end date.", exception.getMessage());
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().review());
    }

    @Test
    void setPlanReview_beforeScheduledTime_rejectsWithActionableMessage() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.setPlanReview(trip.id(), plan.id(), ratingReview(4)));

        assertEquals("Plan can be reviewed only after its scheduled time.", exception.getMessage());
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().plans().get(0).review());
    }

    @Test
    void reviewOperations_missingTargets_rejectWithoutSaving() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));

        assertThrows(IllegalArgumentException.class,
                () -> service.setTripReview(UUID.randomUUID(), ratingReview(4)));
        assertThrows(IllegalArgumentException.class,
                () -> service.removeTripReview(UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class,
                () -> service.setPlanReview(trip.id(), UUID.randomUUID(), ratingReview(4)));
        assertThrows(IllegalArgumentException.class,
                () -> service.removePlanReview(trip.id(), UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class,
                () -> service.setPlanReview(UUID.randomUUID(), UUID.randomUUID(), ratingReview(4)));
        assertThrows(IllegalArgumentException.class,
                () -> service.removePlanReview(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void removeAbsentReviews_isIdempotentWithoutRepositoryWrite() {
        FailingTripRepository repository = new FailingTripRepository();
        DoggoService service = new DoggoService(repository,
                Clock.fixed(Instant.parse("2027-01-05T10:00:00Z"), ZoneOffset.UTC));
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan plan = service.addPlan(trip.id(), "Museum", LocalDate.of(2027, 1, 4),
                LocalTime.of(9, 0));
        Trip storedTrip = service.getTrip(trip.id()).orElseThrow();
        repository.setShouldFail(true);

        assertEquals(storedTrip, service.removeTripReview(trip.id()));
        assertEquals(plan, service.removePlanReview(trip.id(), plan.id()));
    }

    @Test
    void editTrip_reviewedTrip_preservesReviewWhenStillPast() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Review review = new Review(OptionalInt.of(5), Optional.of("Wonderful."));
        service.setTripReview(trip.id(), review);

        Trip updatedTrip = service.editTrip(trip.id(), " Korea ", LocalDate.of(2027, 1, 2),
                LocalDate.of(2027, 1, 4));

        assertEquals("Korea", updatedTrip.title());
        assertEquals(Optional.of(review), updatedTrip.review());
        assertEquals(updatedTrip, service.getTrip(trip.id()).orElseThrow());
    }

    @Test
    void editTrip_reviewedTrip_movingOutOfPastIsRejected() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.setTripReview(trip.id(), ratingReview(5));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.editTrip(trip.id(), "Japan", LocalDate.of(2027, 1, 1),
                        LocalDate.of(2027, 1, 5)));

        assertEquals("A reviewed Trip must remain past after editing.", exception.getMessage());
        assertEquals(LocalDate.of(2027, 1, 4), service.getTrip(trip.id()).orElseThrow().endDate());
        assertTrue(service.getTrip(trip.id()).orElseThrow().review().isPresent());
    }

    @Test
    void editPlan_reviewedPlan_preservesReviewWhenStillComplete() {
        DoggoService service = serviceAt("2027-01-05T10:00:00Z");
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Review review = new Review(OptionalInt.empty(), Optional.of("Worth revisiting."));
        service.setPlanReview(trip.id(), plan.id(), review);

        Plan updatedPlan = service.editPlan(trip.id(), plan.id(), "Gallery",
                LocalDate.of(2027, 1, 5), LocalTime.of(10, 0));

        assertEquals("Gallery", updatedPlan.destination());
        assertEquals(Optional.of(review), updatedPlan.review());
        assertEquals(updatedPlan, service.getTrip(trip.id()).orElseThrow().plans().get(0));
    }

    @Test
    void editPlan_reviewedPlan_movingAfterNowIsRejected() {
        DoggoService service = serviceAt("2027-01-05T10:00:00Z");
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        service.setPlanReview(trip.id(), plan.id(), ratingReview(4));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.editPlan(trip.id(), plan.id(), "Gallery", LocalDate.of(2027, 1, 5),
                        LocalTime.of(11, 0)));

        assertEquals("A reviewed Plan cannot be moved after its completion time.",
                exception.getMessage());
        Plan storedPlan = service.getTrip(trip.id()).orElseThrow().plans().get(0);
        assertEquals(Optional.of(ratingReview(4)), storedPlan.review());
        assertEquals(plan.destination(), storedPlan.destination());
        assertEquals(plan.date(), storedPlan.date());
        assertEquals(plan.time(), storedPlan.time());
    }

    @Test
    void setTripReview_failedSave_preservesPreviouslyStoredTrip() {
        FailingTripRepository repository = new FailingTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        repository.setShouldFail(true);

        assertThrows(RepositoryException.class,
                () -> service.setTripReview(trip.id(), ratingReview(4)));

        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().review());
    }

    @Test
    void removeTripReview_failedSave_preservesPreviouslyStoredReview() {
        FailingTripRepository repository = new FailingTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Review review = service.setTripReview(trip.id(), ratingReview(4)).review().orElseThrow();
        repository.setShouldFail(true);

        assertThrows(RepositoryException.class, () -> service.removeTripReview(trip.id()));

        assertEquals(Optional.of(review), service.getTrip(trip.id()).orElseThrow().review());
    }

    @Test
    void setPlanReview_failedSave_preservesPreviouslyStoredPlan() {
        FailingTripRepository repository = new FailingTripRepository();
        DoggoService service = serviceAt(repository, "2027-01-05T10:00:00Z");
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        repository.setShouldFail(true);

        assertThrows(RepositoryException.class,
                () -> service.setPlanReview(trip.id(), plan.id(), ratingReview(4)));

        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().plans().get(0).review());
    }

    @Test
    void removePlanReview_failedSave_preservesPreviouslyStoredReview() {
        FailingTripRepository repository = new FailingTripRepository();
        DoggoService service = serviceAt(repository, "2027-01-05T10:00:00Z");
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Review review = service.setPlanReview(trip.id(), plan.id(), ratingReview(4)).review()
                .orElseThrow();
        repository.setShouldFail(true);

        assertThrows(RepositoryException.class,
                () -> service.removePlanReview(trip.id(), plan.id()));

        assertEquals(Optional.of(review), service.getTrip(trip.id()).orElseThrow().plans().get(0).review());
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

    private static DoggoService serviceAt(String instant) {
        return new DoggoService(new InMemoryTripRepository(),
                Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    private static DoggoService serviceAt(FailingTripRepository repository, String instant) {
        return new DoggoService(repository, Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    private static Review ratingReview(int rating) {
        return new Review(OptionalInt.of(rating), Optional.empty());
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
