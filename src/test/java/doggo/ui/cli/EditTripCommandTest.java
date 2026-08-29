package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import doggo.application.DoggoService;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class EditTripCommandTest {
    @Test
    void execute_changedTitle_updatesTripAndRefreshesList() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = CommandTestHelper.context(service, "Japan revised\n\n\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new EditTripCommand(1).execute(context);

        assertEquals("Japan revised", service.getTrip(trip.id()).orElseThrow().title());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("Trip updated."));
        assertTrue(result.message().contains("Japan revised"));
        assertFalse(result.shouldExit());
    }
}
