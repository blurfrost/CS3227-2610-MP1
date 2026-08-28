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

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.domain.Trip;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class GalleryMaintenanceTest {
    @Test
    void editGalleryTrip_sameStatus_refreshesGalleryAndUpdatesTrip() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "Japan revised\n\n\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new EditTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("Trip updated."));
        assertTrue(result.message().contains("Japan revised"));
        assertEquals("Japan revised", service.getTrip(trip.id()).orElseThrow().title());
    }

    @Test
    void editGalleryTrip_noChanges_refreshesGalleryWithoutMutation() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "\n\n\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new EditTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("No changes made."));
        assertTrue(result.message().contains("Japan"));
        assertEquals(trip, service.getTrip(trip.id()).orElseThrow());
    }

    @Test
    void editGalleryTrip_invalidDateRepromptsAndRefreshesGallery() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "\n31/02/2027\n\n\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new EditTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("No changes made."));
        assertTrue(result.message().contains("Japan"));
        assertEquals(trip, service.getTrip(trip.id()).orElseThrow());
    }

    @Test
    void editTrip_acrossStatusBoundary_routesToResultingList() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip past = service.createTrip("Past", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Trip current = service.createTrip("Current", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));

        CliContext galleryContext = createContext(service, "Past active\n\n05/01/2027\n");
        galleryContext.session().enterGallery();
        galleryContext.galleryMenu();
        CommandResult pastResult = new EditTripCommand(1).execute(galleryContext);

        CliContext organiseContext = createContext(service, "Current past\n01/01/2027\n04/01/2027\n");
        organiseContext.session().enterOrganise();
        organiseContext.organiseMenu();
        CommandResult currentResult = new EditTripCommand(2).execute(organiseContext);

        assertEquals(CliMode.ORGANISE, galleryContext.session().mode());
        assertTrue(pastResult.message().contains("Past active"));
        assertEquals(CliMode.GALLERY, organiseContext.session().mode());
        assertTrue(currentResult.message().contains("Current past"));
        assertEquals(LocalDate.of(2027, 1, 5), service.getTrip(past.id()).orElseThrow().endDate());
        assertEquals(LocalDate.of(2027, 1, 4),
                service.getTrip(current.id()).orElseThrow().endDate());
    }

    @Test
    void editGalleryTrip_deletedTarget_doesNotPromptOrMutate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createNoPromptContext(service);
        context.session().enterGallery();
        context.galleryMenu();
        repository.delete(trip.id());

        CommandResult result = new EditTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("The selected Trip is no longer available."));
        assertEquals(CliMode.GALLERY, context.session().mode());
    }

    @Test
    void editGalleryTrip_lateServiceFailure_refreshesGalleryAndStaysInGallery() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        BufferedReader input = new BufferedReader(new StringReader("Japan revised\n\n\n")) {
            private int linesRead;

            @Override
            public String readLine() throws IOException {
                String line = super.readLine();
                linesRead++;
                if (linesRead == 3) {
                    repository.delete(trip.id());
                }
                return line;
            }
        };
        CliSession session = new CliSession();
        CliContext context = new CliContext(service, session,
                new CliPrompter(input, writer), new CliFormatter(), writer);
        session.enterGallery();
        context.galleryMenu();

        CommandResult result = new EditTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, session.mode());
        assertTrue(result.message().contains("[MODE: GALLERY]"));
        assertTrue(output.toString().contains("Error: Trip not found."));
        assertTrue(service.getTrip(trip.id()).isEmpty());
    }

    @Test
    void deleteGalleryTrip_confirmed_removesAggregateAndRefreshesGallery() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));
        CliContext context = createContext(service, "yes\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new DeleteTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("Trip deleted."));
        assertTrue(result.message().contains("There are no past Trips."));
        assertTrue(service.getTrip(trip.id()).isEmpty());
    }

    @Test
    void deleteGalleryTrip_cancelled_preservesTripAndRefreshesGallery() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "no\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new DeleteTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("Trip deletion cancelled."));
        assertTrue(result.message().contains("Japan (from 01/01/2027 to 04/01/2027)"));
        assertEquals(trip, service.getTrip(trip.id()).orElseThrow());
    }

    @Test
    void deleteGalleryTrip_invalidConfirmation_repromptsUntilCancellation() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        CliContext context = new CliContext(service, new CliSession(),
                new CliPrompter(new BufferedReader(new StringReader("maybe\nYes\nno\n")), writer),
                new CliFormatter(), writer);
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new DeleteTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(output.toString().contains("Please enter exactly yes or no."));
        assertTrue(result.message().contains("Trip deletion cancelled."));
        assertEquals(trip, service.getTrip(trip.id()).orElseThrow());
    }

    @Test
    void deleteGalleryTrip_deletedTarget_doesNotPromptOrMutate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createNoPromptContext(service);
        context.session().enterGallery();
        context.galleryMenu();
        repository.delete(trip.id());

        CommandResult result = new DeleteTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("The selected Trip is no longer available."));
    }

    @Test
    void deleteGalleryTrip_reclassifiedTarget_doesNotPromptOrMutate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createNoPromptContext(service);
        context.session().enterGallery();
        context.galleryMenu();
        repository.save(new Trip(trip.id(), trip.title(), LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5)));

        CommandResult result = new DeleteTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("The selected Trip is no longer available."));
        assertTrue(service.getTrip(trip.id()).isPresent());
    }

    @Test
    void deleteGalleryTrip_deletedAfterConfirmation_failsSafelyAndRefreshesGallery() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        BufferedReader input = new BufferedReader(new StringReader("yes\n")) {
            @Override
            public String readLine() throws IOException {
                String confirmation = super.readLine();
                repository.delete(trip.id());
                return confirmation;
            }
        };
        CliContext context = new CliContext(service, new CliSession(),
                new CliPrompter(input, writer), new CliFormatter(), writer);
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new DeleteTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("Error: Trip not found."));
        assertTrue(result.message().contains("There are no past Trips."));
    }

    @Test
    void editTrip_reclassifiedTarget_doesNotPromptInEitherList() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip past = service.createTrip("Past", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Trip current = service.createTrip("Current", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));
        CliContext galleryContext = createNoPromptContext(service);
        galleryContext.session().enterGallery();
        galleryContext.galleryMenu();
        repository.save(new Trip(past.id(), past.title(), LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5)));
        CliContext organiseContext = createNoPromptContext(service);
        organiseContext.session().enterOrganise();
        organiseContext.organiseMenu();
        repository.save(new Trip(current.id(), current.title(), LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4)));

        CommandResult galleryResult = new EditTripCommand(1).execute(galleryContext);
        CommandResult organiseResult = new EditTripCommand(1).execute(organiseContext);

        assertTrue(galleryResult.message().contains("The selected Trip is no longer available."));
        assertTrue(organiseResult.message().contains("The selected Trip is no longer available."));
        assertEquals(CliMode.GALLERY, galleryContext.session().mode());
        assertEquals(CliMode.ORGANISE, organiseContext.session().mode());
    }

    private static CliContext createContext(DoggoService service, String input) {
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        return new CliContext(service, new CliSession(),
                new CliPrompter(new BufferedReader(new StringReader(input)), writer),
                new CliFormatter(), writer);
    }

    private static CliContext createNoPromptContext(DoggoService service) {
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        BufferedReader input = new BufferedReader(new StringReader("")) {
            @Override
            public String readLine() {
                throw new AssertionError("A stale target must not prompt for input.");
            }
        };
        return new CliContext(service, new CliSession(), new CliPrompter(input, writer),
                new CliFormatter(), writer);
    }
}
