import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CliSessionTest {
    @Test
    void enterTrip_setsTripModeAndSelectedTrip() {
        CliSession session = new CliSession();
        UUID tripId = UUID.randomUUID();

        session.enterTrip(tripId);

        assertEquals(CliMode.TRIP, session.mode());
        assertEquals(tripId, session.selectedTripId().orElseThrow());
    }

    @Test
    void enterOrganise_clearsTripSelectionAndDisplayedTrips() {
        CliSession session = new CliSession();
        session.enterTrip(UUID.randomUUID());
        session.setDisplayedTripIds(List.of(UUID.randomUUID()));

        session.enterOrganise();

        assertEquals(CliMode.ORGANISE, session.mode());
        assertFalse(session.selectedTripId().isPresent());
        assertFalse(session.tripIdAt(1).isPresent());
    }

    @Test
    void enterMain_clearsTripSelectionAndDisplayedTrips() {
        CliSession session = new CliSession();
        session.enterTrip(UUID.randomUUID());
        session.setDisplayedTripIds(List.of(UUID.randomUUID()));

        session.enterMain();

        assertEquals(CliMode.MAIN, session.mode());
        assertFalse(session.selectedTripId().isPresent());
        assertFalse(session.tripIdAt(1).isPresent());
    }

    @Test
    void enterTrip_nullTripId_throwsException() {
        CliSession session = new CliSession();

        assertThrows(NullPointerException.class, () -> session.enterTrip(null));
    }
}
