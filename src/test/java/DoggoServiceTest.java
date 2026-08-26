import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class DoggoServiceTest {
    @Test
    void createTrip_andGetTrips_returnsTripsInStartDateOrder() {
        DoggoService service = new DoggoService(new InMemoryTripRepository());
        service.createTrip("Later", LocalDate.of(2028, 1, 1), LocalDate.of(2028, 1, 2));
        service.createTrip("Earlier", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2));

        assertEquals("Earlier", service.getTrips().get(0).title());
        assertEquals("Later", service.getTrips().get(1).title());
    }

    @Test
    void addPlan_toExistingTrip_storesPlan() {
        DoggoService service = new DoggoService(new InMemoryTripRepository());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));

        service.addPlan(trip.id(), "Mount Fuji", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));

        assertEquals(1, service.getTrip(trip.id()).orElseThrow().plans().size());
    }

    @Test
    void getPlans_unsortedPlans_returnsChronologicalOrder() {
        DoggoService service = new DoggoService(new InMemoryTripRepository());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        Plan laterDate = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "Osaka", LocalDate.of(2027, 1, 6), LocalTime.of(9, 0));
        Plan laterTime = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.of(10, 0));
        Plan earlierId = new Plan(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Mount Fuji", LocalDate.of(2027, 1, 5), LocalTime.of(10, 0));
        trip.addPlan(laterDate);
        trip.addPlan(laterTime);
        trip.addPlan(earlierId);

        assertEquals(List.of(earlierId, laterTime, laterDate), service.getPlans(trip));
    }
}
