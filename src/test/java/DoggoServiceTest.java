import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

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
}
