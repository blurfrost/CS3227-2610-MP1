package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import doggo.application.DoggoService;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class DeleteTripCommandTest {
    @Test
    void execute_confirmedDeletion_removesTripAndRefreshesList() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = CommandTestHelper.context(service, "yes\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new DeleteTripCommand(1).execute(context);

        assertTrue(service.getTrip(trip.id()).isEmpty());
        assertTrue(result.message().contains("Trip deleted."));
        assertTrue(result.message().contains("There are no past Trips."));
        assertFalse(result.shouldExit());
    }
}
