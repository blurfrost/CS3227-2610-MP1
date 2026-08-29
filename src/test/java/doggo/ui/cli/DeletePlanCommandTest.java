package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class DeletePlanCommandTest {
    @Test
    void execute_confirmedDeletion_removesPlanAndRefreshesTrip() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Museum", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        CliContext context = CommandTestHelper.context(service, "yes\n");
        context.session().enterTrip(trip.id());
        context.selectedTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new DeletePlanCommand(1).execute(context);

        assertTrue(service.getTrip(trip.id()).orElseThrow().plans().stream()
                .noneMatch(candidate -> candidate.id().equals(plan.id())));
        assertTrue(result.message().contains("Plan deleted."));
        assertTrue(result.message().contains("Viewing: Japan"));
        assertFalse(result.shouldExit());
    }
}
