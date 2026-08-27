import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CliContextTest {
    @Test
    void deletePlan_usesDisplayedPlanAfterRepositoryOrderChanges() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository);
        UUID tripId = UUID.randomUUID();
        UUID firstPlanId = UUID.randomUUID();
        UUID secondPlanId = UUID.randomUUID();
        Trip trip = new Trip(tripId, "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan firstPlan = new Plan(firstPlanId, "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        Plan secondPlan = new Plan(secondPlanId, "Osaka", LocalDate.of(2027, 1, 6),
                LocalTime.of(9, 0));
        repository.save(trip.withAddedPlan(firstPlan).withAddedPlan(secondPlan));
        CliSession session = new CliSession();
        session.enterTrip(tripId);
        StringWriter output = new StringWriter();
        CliContext context = new CliContext(service, session,
                new CliPrompter(new BufferedReader(new StringReader("yes\n")),
                        new PrintWriter(output)), new CliFormatter(), new PrintWriter(output));

        context.selectedTripView(service.getTrip(tripId).orElseThrow());
        Trip reorderedTrip = new Trip(tripId, "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan movedFirstPlan = new Plan(firstPlanId, "Tokyo", LocalDate.of(2027, 1, 7),
                LocalTime.of(9, 0));
        repository.save(reorderedTrip.withAddedPlan(movedFirstPlan).withAddedPlan(secondPlan));

        new DeletePlanCommand(1).execute(context);

        assertEquals(List.of(secondPlan), service.getTrip(tripId).orElseThrow().plans());
    }
}
