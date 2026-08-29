package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class NewPlanCommandTest {
    @Test
    void execute_validInput_addsPlanAndStaysInSelectedTrip() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        CliContext context = CommandTestHelper.context(service,
                "Museum\n05/01/2027\n09:00\n");
        context.session().enterTrip(trip.id());

        CommandResult result = new NewPlanCommand().execute(context);

        Plan plan = service.getTrip(trip.id()).orElseThrow().plans().get(0);
        assertEquals("Museum", plan.destination());
        assertEquals(LocalDate.of(2027, 1, 5), plan.date());
        assertEquals(LocalTime.of(9, 0), plan.time());
        assertEquals(CliMode.TRIP, context.session().mode());
        assertTrue(result.message().contains("Plan created!"));
        assertFalse(result.shouldExit());
    }
}
