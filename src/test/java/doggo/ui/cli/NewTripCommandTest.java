package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import doggo.application.DoggoService;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class NewTripCommandTest {
    @Test
    void execute_validInput_createsTripAndRoutesToTripList() {
        DoggoService service = CommandTestHelper.service();
        CliContext context = CommandTestHelper.context(service,
                "Japan\n01/01/2027\n04/01/2027\n");

        CommandResult result = new NewTripCommand().execute(context);

        Trip createdTrip = service.getTrips().get(0);
        assertEquals("Japan", createdTrip.title());
        assertEquals(LocalDate.of(2027, 1, 1), createdTrip.startDate());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("Trip successfully added!"));
        assertTrue(result.message().contains("Japan"));
        assertFalse(result.shouldExit());
    }

    @Test
    void execute_overLimitTitle_repromptsAndCreatesTrip() {
        DoggoService service = CommandTestHelper.service();
        String overLimitTitle = "a".repeat(Trip.MAX_TITLE_LENGTH + 1);
        CliContext context = CommandTestHelper.context(service,
                overLimitTitle + "\nJapan\n01/01/2027\n04/01/2027\n");

        CommandResult result = new NewTripCommand().execute(context);

        assertEquals("Japan", service.getTrips().getFirst().title());
        assertTrue(result.message().contains("Trip successfully added!"));
        assertFalse(result.shouldExit());
    }
}
