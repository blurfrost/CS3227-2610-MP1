package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InvalidIndexCommandTest {
    @Test
    void execute_withoutDisplayedTrips_reportsNoTripsAndCurrentView() {
        CliContext context = CommandTestHelper.context();
        context.session().enterOrganise();

        CommandResult result = new InvalidIndexCommand("view", IndexedEntity.TRIP)
                .execute(context);

        assertTrue(result.message().contains("There are no trips to view."));
        assertTrue(result.message().contains("[MODE: ORGANISE]"));
        assertFalse(result.shouldExit());
    }

    @Test
    void execute_withDisplayedPlans_reportsPlanRange() {
        CliContext context = CommandTestHelper.context();
        context.session().enterDashboard();
        context.session().setDisplayedPlanTargets(java.util.List.of(
                new PlanTarget(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()),
                new PlanTarget(java.util.UUID.randomUUID(), java.util.UUID.randomUUID())));

        CommandResult result = new InvalidIndexCommand("edit", IndexedEntity.PLAN)
                .execute(context);

        assertTrue(result.message().contains("Plan number should be from 1 to 2."));
        assertTrue(result.message().contains("[MODE: DASHBOARD]"));
    }
}
