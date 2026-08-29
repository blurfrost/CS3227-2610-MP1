package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class BackCommandTest {
    @Test
    void execute_fromTrip_returnsToOrganise() {
        CliContext context = CommandTestHelper.context();
        Trip trip = context.service().createTrip("Trip", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));
        context.session().enterTrip(trip.id());

        CommandResult result = new BackCommand().execute(context);

        assertEquals(CliMode.ORGANISE, context.session().mode());
        assertTrue(result.message().contains("[MODE: ORGANISE]"));
    }

    @Test
    void execute_fromGalleryTrip_returnsToGallery() {
        CliContext context = CommandTestHelper.context();
        Trip trip = context.service().createTrip("Past trip", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        context.session().enterGalleryTrip(trip.id());

        CommandResult result = new BackCommand().execute(context);

        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("[MODE: GALLERY]"));
    }

    @Test
    void execute_fromTopLevelMode_returnsToMain() {
        CliContext context = CommandTestHelper.context();
        context.session().enterDashboard();

        CommandResult result = new BackCommand().execute(context);

        assertEquals(CliMode.MAIN, context.session().mode());
        assertTrue(result.message().contains("Welcome!"));
    }
}
