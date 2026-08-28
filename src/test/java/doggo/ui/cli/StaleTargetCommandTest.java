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

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Trip;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class StaleTargetCommandTest {
    @Test
    void deleteTrip_staleDisplayedTarget_doesNotPromptForConfirmation() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2));
        CliContext context = createContext(service, "");
        context.organiseMenu();
        repository.delete(trip.id());

        CommandResult result = new DeleteTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("The selected Trip is no longer available."));
    }

    @Test
    void newPlan_missingSelectedTrip_doesNotPromptForDestination() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2));
        CliContext context = createContext(service, "");
        context.session().enterTrip(trip.id());
        repository.delete(trip.id());

        CommandResult result = new NewPlanCommand().execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("Selected Trip could not be found."));
        assertTrue(result.message().contains("[MODE: ORGANISE]"));
    }

    @Test
    void editTrip_staleDisplayedTarget_doesNotPromptForFields() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2));
        CliContext context = createContext(service, "");
        context.organiseMenu();
        repository.delete(trip.id());

        CommandResult result = new EditTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("The selected Trip is no longer available."));
    }

    @Test
    void editPlan_staleSelectedTrip_doesNotPromptForFields() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 1), LocalTime.of(9, 0));
        CliContext context = createContext(service, "");
        context.session().enterTrip(trip.id());
        context.selectedTripView(service.getTrip(trip.id()).orElseThrow());
        repository.delete(trip.id());

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("Selected Trip could not be found."));
    }

    @Test
    void editPlan_staleDashboardTrip_doesNotPromptForFields() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));
        CliContext context = createContext(service, "");
        context.session().enterDashboard();
        context.dashboardMenu();
        repository.delete(trip.id());

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.DASHBOARD, context.session().mode());
        assertTrue(result.message().contains("Selected Trip could not be found."));
        assertTrue(result.message().contains("[MODE: DASHBOARD]"));
    }

    @Test
    void editPlan_staleDashboardPlan_doesNotPromptForFields() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));
        CliContext context = createContext(service, "");
        context.session().enterDashboard();
        context.dashboardMenu();
        repository.save(new Trip(trip.id(), trip.title(), trip.startDate(), trip.endDate()));

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.DASHBOARD, context.session().mode());
        assertTrue(result.message().contains("The selected Plan is no longer available."));
        assertTrue(result.message().contains("[MODE: DASHBOARD]"));
        assertTrue(service.getTrip(trip.id()).orElseThrow().plans().isEmpty());
    }

    @Test
    void editPlan_dashboardTargetBecomesStaleAfterPrompts_doesNotMutatePlan() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        BufferedReader input = new BufferedReader(new StringReader(
                "Kyoto\n05/01/2027\n10:00\n")) {
            private int linesRead;

            @Override
            public String readLine() throws IOException {
                String line = super.readLine();
                linesRead++;
                if (linesRead == 3) {
                    repository.save(new Trip(trip.id(), trip.title(), trip.startDate(), trip.endDate()));
                }
                return line;
            }
        };
        CliSession session = new CliSession();
        CliContext context = new CliContext(service, session,
                new CliPrompter(input, writer), new CliFormatter(), writer);
        session.enterDashboard();
        context.dashboardMenu();

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("Plan not found."));
        assertTrue(result.message().contains("[MODE: DASHBOARD]"));
        assertTrue(service.getTrip(trip.id()).orElseThrow().plans().isEmpty());
    }

    @Test
    void deletePlan_staleDisplayedTarget_doesNotPromptForConfirmation() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 1), LocalTime.of(9, 0));
        Plan retainedPlan = service.addPlan(trip.id(), "Osaka", LocalDate.of(2027, 1, 2), LocalTime.of(10, 0));
        CliContext context = createContext(service, "");
        context.session().enterTrip(trip.id());
        context.selectedTripView(service.getTrip(trip.id()).orElseThrow());
        repository.save(new Trip(trip.id(), trip.title(), trip.startDate(), trip.endDate())
                .withAddedPlan(retainedPlan));

        CommandResult result = new DeletePlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("The selected Plan is no longer available."));
        assertEquals(List.of(retainedPlan), service.getTrip(trip.id()).orElseThrow().plans());
    }

    @Test
    void editCommands_refreshMappingsAfterSortOrderChanges() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip japan = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 20));
        Trip korea = service.createTrip("Korea", LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 6));
        String input = String.join("\n", "Japan moved", "10/01/2027", "",
                "Korea moved", "", "") + "\n";
        CliContext context = createContext(service, input);
        context.organiseMenu();

        new EditTripCommand(1).execute(context);
        new EditTripCommand(1).execute(context);

        assertEquals("Japan moved", service.getTrip(japan.id()).orElseThrow().title());
        assertEquals("Korea moved", service.getTrip(korea.id()).orElseThrow().title());
    }

    private static CliContext createContext(DoggoService service, String input) {
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        return new CliContext(service, new CliSession(),
                new CliPrompter(new BufferedReader(new StringReader(input)), writer),
                new CliFormatter(), writer);
    }
}
