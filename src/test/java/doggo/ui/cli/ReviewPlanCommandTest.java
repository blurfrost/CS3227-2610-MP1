package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.OptionalInt;

import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class ReviewPlanCommandTest {
    @Test
    void execute_dashboardTarget_persistsReviewAndRefreshesDashboard() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.MIDNIGHT);
        CliContext context = CommandTestHelper.context(service, "5\nGreat activity.\n");
        context.session().enterDashboard();
        context.dashboardMenu();

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertEquals(Optional.of(new Review(OptionalInt.of(5), Optional.of("Great activity."))),
                service.getTrip(trip.id()).orElseThrow().plans().stream()
                        .filter(candidate -> candidate.id().equals(plan.id()))
                        .findFirst().orElseThrow().review());
        assertTrue(result.message().contains("Review added."));
        assertTrue(result.message().contains("Great activity."));
        assertFalse(result.shouldExit());
    }

    @Test
    void execute_planScheduledLater_rejectsReviewBeforePrompt() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        CliContext context = CommandTestHelper.context(service, "5\nShould not be used\n");
        context.session().enterDashboard();
        context.dashboardMenu();

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertTrue(result.message().contains("Plan can be reviewed only after its scheduled time."));
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().plans().stream()
                .filter(candidate -> candidate.id().equals(plan.id()))
                .findFirst().orElseThrow().review());
        assertFalse(result.shouldExit());
    }
}
