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

class EditPlanCommandTest {
    @Test
    void execute_changedDetails_updatesPlanAndStaysInSelectedTrip() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        CliContext context = CommandTestHelper.context(service,
                "Museum updated\n\n10:00\n");
        context.session().enterTrip(trip.id());
        context.selectedTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new EditPlanCommand(1).execute(context);

        Plan updatedPlan = service.getTrip(trip.id()).orElseThrow().plans().get(0);
        assertEquals(plan.id(), updatedPlan.id());
        assertEquals("Museum updated", updatedPlan.destination());
        assertEquals(LocalTime.of(10, 0), updatedPlan.time());
        assertEquals(CliMode.TRIP, context.session().mode());
        assertTrue(result.message().contains("Plan updated."));
        assertFalse(result.shouldExit());
    }
}
