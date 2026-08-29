package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class PlanReviewTest {
    @Test
    void reviewPlan_dashboardAddsReviewAndRefreshesDashboard() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.MIDNIGHT);
        CliContext context = createContext(service, "5\nGreat activity.\n");
        context.session().enterDashboard();
        context.dashboardMenu();

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("Review added."));
        assertTrue(result.message().contains("   Rating: 5/5\n"));
        assertTrue(result.message().contains("   Review: Great activity.\n"));
        assertEquals(Optional.of(new Review(OptionalInt.of(5), Optional.of("Great activity."))),
                service.getTrip(trip.id()).orElseThrow().plans().get(0).review());
        assertEquals(plan.id(), context.session().planTargetAt(1).orElseThrow().planId());
    }

    @Test
    void reviewPlan_dashboardUsesRetainedCompositeTargetAcrossTrips() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip japan = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Trip korea = service.createTrip("Korea", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan japanPlan = service.addPlan(japan.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.MIDNIGHT);
        Plan koreaPlan = service.addPlan(korea.id(), "Seoul", LocalDate.of(2027, 1, 5),
                LocalTime.MIDNIGHT);
        CliContext context = createContext(service, "4\nWorth revisiting.\n");
        context.session().enterDashboard();
        context.dashboardMenu();

        CommandResult result = new ReviewPlanCommand(2).execute(context);

        assertTrue(result.message().contains("Review added."));
        assertEquals(Optional.empty(), service.getTrip(japan.id()).orElseThrow().plans().get(0).review());
        assertEquals(Optional.of(new Review(OptionalInt.of(4), Optional.of("Worth revisiting."))),
                service.getTrip(korea.id()).orElseThrow().plans().get(0).review());
        assertEquals(japanPlan.id(), context.session().planTargetAt(1).orElseThrow().planId());
        assertEquals(koreaPlan.id(), context.session().planTargetAt(2).orElseThrow().planId());
    }

    @Test
    void reviewPlan_dashboardLaterTodayFailsBeforePrompt() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));
        CliContext context = createContext(service, "5\nShould not be consumed\n");
        context.session().enterDashboard();
        context.dashboardMenu();

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertTrue(result.message().contains("Plan can be reviewed only after its scheduled time."));
        assertFalse(contextOutput(context).contains("Enter rating"));
    }

    @Test
    void reviewPlan_selectedTripViewsSupportReviewingCompletedPlans() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip currentTrip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan currentTripPlan = service.addPlan(currentTrip.id(), "Tokyo", LocalDate.of(2027, 1, 4),
                LocalTime.of(9, 0));
        CliContext tripContext = createContext(service, "3\nGreat plan.\n");
        tripContext.session().enterTrip(currentTrip.id());
        tripContext.selectedTripView(service.getTrip(currentTrip.id()).orElseThrow());

        CommandResult tripResult = new ReviewPlanCommand(1).execute(tripContext);

        Trip pastTrip = service.createTrip("Korea", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan pastTripPlan = service.addPlan(pastTrip.id(), "Seoul", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));
        CliContext galleryContext = createContext(service, "4\nGreat gallery plan.\n");
        galleryContext.session().enterGalleryTrip(pastTrip.id());
        galleryContext.selectedGalleryTripView(service.getTrip(pastTrip.id()).orElseThrow());

        CommandResult galleryResult = new ReviewPlanCommand(1).execute(galleryContext);

        assertTrue(tripResult.message().contains("Review added."));
        assertTrue(galleryResult.message().contains("Review added."));
        assertEquals(Optional.of(new Review(OptionalInt.of(3), Optional.of("Great plan."))),
                service.getTrip(currentTrip.id()).orElseThrow().plans().stream()
                        .filter(plan -> plan.id().equals(currentTripPlan.id()))
                        .findFirst().orElseThrow().review());
        assertEquals(Optional.of(new Review(OptionalInt.of(4), Optional.of("Great gallery plan."))),
                service.getTrip(pastTrip.id()).orElseThrow().plans().stream()
                        .filter(plan -> plan.id().equals(pastTripPlan.id()))
                        .findFirst().orElseThrow().review());
    }

    @Test
    void reviewPlan_existingReviewSupportsPreservationAndRemoval() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 4),
                LocalTime.of(9, 0));
        service.setPlanReview(trip.id(), plan.id(),
                new Review(OptionalInt.of(4), Optional.of("Good activity.")));
        CliContext context = createContext(service, "\nBetter activity.\n-\n-\n");
        context.session().enterTrip(trip.id());
        context.selectedTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult updateResult = new ReviewPlanCommand(1).execute(context);
        CommandResult removeResult = new ReviewPlanCommand(1).execute(context);

        assertTrue(updateResult.message().contains("Review updated."));
        assertTrue(removeResult.message().contains("Review removed."));
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().plans().get(0).review());
    }

    @Test
    void reviewPlan_endOfInputDoesNotMutate() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 4), LocalTime.of(9, 0));
        CliContext context = createContext(service, "5\n");
        context.session().enterTrip(trip.id());
        context.selectedTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertTrue(result.shouldExit());
        assertEquals("Bye!", result.message());
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().plans().get(0).review());
    }

    @Test
    void reviewPlan_selectedModeOwnershipMismatchDoesNotPrompt() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip selectedTrip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Trip otherTrip = service.createTrip("Korea", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan otherPlan = service.addPlan(otherTrip.id(), "Seoul", LocalDate.of(2027, 1, 4),
                LocalTime.of(9, 0));
        CliContext context = createContext(service, "5\nShould not be consumed\n");
        context.session().enterTrip(selectedTrip.id());
        context.selectedTripView(service.getTrip(selectedTrip.id()).orElseThrow());
        context.session().setDisplayedPlanTargets(
                List.of(new PlanTarget(otherTrip.id(), otherPlan.id())));

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Plan is no longer available."));
        assertFalse(contextOutput(context).contains("Enter rating"));
    }

    @Test
    void reviewPlan_staleDashboardTargetBeforePromptDoesNotPrompt() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.MIDNIGHT);
        CliContext context = createContext(service, "5\nShould not be consumed\n");
        context.session().enterDashboard();
        context.dashboardMenu();
        repository.save(new Trip(trip.id(), trip.title(), trip.startDate(), trip.endDate()));

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Plan is no longer available."));
        assertFalse(contextOutput(context).contains("Enter rating"));
    }

    @Test
    void reviewPlan_dashboardTargetMovedOffTodayBeforePromptDoesNotPrompt() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.MIDNIGHT);
        CliContext context = createContext(service, "5\nShould not be consumed\n");
        context.session().enterDashboard();
        context.dashboardMenu();
        repository.save(trip.withAddedPlan(plan.withUpdatedDetails(plan.destination(),
                LocalDate.of(2027, 1, 6), plan.time())));

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Plan is no longer available."));
        assertFalse(contextOutput(context).contains("Enter rating"));
        assertTrue(service.getTrip(trip.id()).orElseThrow().plans().get(0).review().isEmpty());
    }

    @Test
    void reviewPlan_staleTargetAfterPromptDoesNotMutate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.MIDNIGHT);
        CliContext context = createContextWithReader(service, new MutatingReader("5\nGreat\n", () ->
                repository.save(new Trip(trip.id(), trip.title(), trip.startDate(), trip.endDate()))));
        context.session().enterDashboard();
        context.dashboardMenu();

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Plan is no longer available."));
        assertTrue(service.getTrip(trip.id()).orElseThrow().plans().isEmpty());
        assertTrue(plan.review().isEmpty());
    }

    @Test
    void reviewPlan_dashboardTargetMovedOffTodayAfterPromptDoesNotMutate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.MIDNIGHT);
        CliContext context = createContextWithReader(service, new MutatingReader("5\nGreat\n", () ->
                repository.save(trip.withAddedPlan(plan.withUpdatedDetails(plan.destination(),
                        LocalDate.of(2027, 1, 6), plan.time())))));
        context.session().enterDashboard();
        context.dashboardMenu();

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Plan is no longer available."));
        assertEquals(LocalDate.of(2027, 1, 6), service.getTrip(trip.id()).orElseThrow()
                .plans().get(0).date());
        assertTrue(service.getTrip(trip.id()).orElseThrow().plans().get(0).review().isEmpty());
    }

    @Test
    void reviewPlan_selectedTripReclassifiedAfterPromptDoesNotMutate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 4),
                LocalTime.of(9, 0));
        CliContext context = createContextWithReader(service, new MutatingReader("5\nGreat\n", () ->
                repository.save(new Trip(trip.id(), trip.title(), LocalDate.of(2027, 1, 1),
                        LocalDate.of(2027, 1, 4)).withAddedPlan(plan))));
        context.session().enterTrip(trip.id());
        context.selectedTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new ReviewPlanCommand(1).execute(context);

        assertTrue(result.message().contains("Selected Trip is no longer available."));
        assertTrue(service.getTrip(trip.id()).orElseThrow().plans().get(0).review().isEmpty());
    }

    private static CliContext createContext(DoggoService service, String input) {
        return createContextWithReader(service, new BufferedReader(new StringReader(input)));
    }

    private static CliContext createContextWithReader(DoggoService service, BufferedReader input) {
        PrintWriter writer = new TestPrintWriter();
        return new CliContext(service, new CliSession(), new CliPrompter(input, writer),
                new CliFormatter(), writer);
    }

    private static String contextOutput(CliContext context) {
        context.output().flush();
        return context.output().toString();
    }

    private static final class TestPrintWriter extends PrintWriter {
        private final StringWriter contents;

        TestPrintWriter() {
            this(new StringWriter());
        }

        private TestPrintWriter(StringWriter contents) {
            super(contents);
            this.contents = contents;
        }

        @Override
        public String toString() {
            flush();
            return contents.toString();
        }
    }

    private static final class MutatingReader extends BufferedReader {
        private final Runnable mutation;
        private int linesRead;

        MutatingReader(String input, Runnable mutation) {
            super(new StringReader(input));
            this.mutation = mutation;
        }

        @Override
        public String readLine() throws IOException {
            String line = super.readLine();
            linesRead++;
            if (linesRead == 2) {
                mutation.run();
            }
            return line;
        }
    }
}
