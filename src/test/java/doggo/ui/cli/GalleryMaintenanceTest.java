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

    @Test
    void newPlanGalleryTrip_createsPlanAndStaysInHistoricalTripView() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "Tokyo\n01/01/2027\n09:00\n");
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(trip);

        CommandResult result = new NewPlanCommand().execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertTrue(result.message().contains("Plan created!"));
        assertTrue(result.message().contains("Tokyo (01/01/2027 at 09:00)"));
        assertEquals(trip.id(), context.session().planTargetAt(1).orElseThrow().tripId());
    }

    @Test
    void newPlanGalleryTrip_acceptsInclusiveEndDate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "Tokyo\n04/01/2027\n09:00\n");
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(trip);

        CommandResult result = new NewPlanCommand().execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("Tokyo (04/01/2027 at 09:00)"));
    }

    @Test
    void newPlanGalleryTrip_missingSelection_doesNotPromptAndReturnsToGallery() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createNoPromptContext(service);
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(trip);
        repository.delete(trip.id());

        CommandResult result = new NewPlanCommand().execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("There are no past Trips."));
    }

    @Test
    void newPlanGalleryTrip_reclassifiedSelection_doesNotPromptAndReturnsToOwningList() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createNoPromptContext(service);
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(trip);
        repository.save(new Trip(trip.id(), trip.title(), LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5)));

        CommandResult result = new NewPlanCommand().execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.ORGANISE, context.session().mode());
        assertTrue(result.message().contains("Japan (from 05/01/2027 to 05/01/2027)"));
    }

    @Test
    void newPlanGalleryTrip_lateDeletionFallsBackWithoutMutatingAnotherTrip() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        BufferedReader input = new BufferedReader(new StringReader("Tokyo\n01/01/2027\n09:00\n")) {
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
        CliContext context = new CliContext(service, new CliSession(),
                new CliPrompter(input, writer), new CliFormatter(), writer);
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(trip);

        CommandResult result = new NewPlanCommand().execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains("Selected Trip is no longer available."));
        assertTrue(service.getTrip(trip.id()).isEmpty());
    }

    @Test
    void editPlanGalleryTrip_updatesPlanAndStaysInHistoricalTripView() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));
        CliContext context = createContext(service, "Kyoto\n02/01/2027\n10:00\n");
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertTrue(result.message().contains("Plan updated."));
        Plan updatedPlan = service.getTrip(trip.id()).orElseThrow().plans().get(0);
        assertEquals(plan.id(), updatedPlan.id());
        assertEquals("Kyoto", updatedPlan.destination());
        assertEquals(LocalDate.of(2027, 1, 2), updatedPlan.date());
        assertEquals(LocalTime.of(10, 0), updatedPlan.time());
    }

    @Test
    void editPlanGalleryTrip_reordersPlansAndRefreshesCompositeTargets() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan firstPlan = service.addPlan(trip.id(), "First", LocalDate.of(2027, 1, 1),
                LocalTime.of(9, 0));
        Plan secondPlan = service.addPlan(trip.id(), "Second", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));
        CliContext context = createContext(service, "First\n03/01/2027\n09:00\n");
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());

        new EditPlanCommand(1).execute(context);

        assertEquals(new PlanTarget(trip.id(), secondPlan.id()),
                context.session().planTargetAt(1).orElseThrow());
        assertEquals(new PlanTarget(trip.id(), firstPlan.id()),
                context.session().planTargetAt(2).orElseThrow());
    }

    @Test
    void editPlanGalleryTrip_reclassifiedSelection_doesNotPromptAndReturnsToOwningList() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2), LocalTime.of(9, 0));
        CliContext context = createNoPromptContext(service);
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());
        repository.save(new Trip(trip.id(), trip.title(), LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5)));

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.ORGANISE, context.session().mode());
        assertTrue(result.message().contains("Selected Trip is no longer available."));
    }

    @Test
    void editPlanGalleryTrip_mismatchedCompositeTarget_doesNotPrompt() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip selectedTrip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Trip otherTrip = service.createTrip("Korea", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan plan = service.addPlan(selectedTrip.id(), "Tokyo", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));
        CliContext context = createNoPromptContext(service);
        context.session().enterGalleryTrip(selectedTrip.id());
        context.selectedGalleryTripView(service.getTrip(selectedTrip.id()).orElseThrow());
        context.session().setDisplayedPlanTargets(List.of(new PlanTarget(otherTrip.id(), plan.id())));

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertTrue(result.message().contains("The selected Plan is no longer available."));
        assertEquals("Tokyo", service.getTrip(selectedTrip.id()).orElseThrow().plans().get(0)
                .destination());
    }

    @Test
    void editPlanGalleryTrip_removedPlan_doesNotPrompt() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2), LocalTime.of(9, 0));
        CliContext context = createNoPromptContext(service);
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());
        repository.save(trip);

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertTrue(result.message().contains("The selected Plan is no longer available."));
    }

    @Test
    void editPlanGalleryTrip_removedAfterPrompts_failsSafelyAndKeepsOtherPlan() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan selectedPlan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));
        Plan retainedPlan = service.addPlan(trip.id(), "Osaka", LocalDate.of(2027, 1, 3),
                LocalTime.of(9, 0));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        BufferedReader input = new BufferedReader(new StringReader(
                "Kyoto\n02/01/2027\n10:00\n")) {
            private int linesRead;

            @Override
            public String readLine() throws IOException {
                String line = super.readLine();
                linesRead++;
                if (linesRead == 3) {
                    repository.save(new Trip(trip.id(), trip.title(), trip.startDate(), trip.endDate())
                            .withAddedPlan(retainedPlan));
                }
                return line;
            }
        };
        CliContext context = new CliContext(service, new CliSession(),
                new CliPrompter(input, writer), new CliFormatter(), writer);
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertTrue(result.message().contains("Plan not found."));
        assertEquals(List.of(retainedPlan), service.getTrip(trip.id()).orElseThrow().plans());
        assertEquals(retainedPlan.id(), context.session().planTargetAt(1).orElseThrow().planId());
    }

    @Test
    void editPlanGalleryTrip_noChanges_refreshesUnchangedDetail() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));
        CliContext context = createContext(service, "\n\n\n");
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new EditPlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertTrue(result.message().contains("No changes made."));
        assertTrue(result.message().contains("Tokyo (02/01/2027 at 09:00)"));
        assertEquals(plan.id(), context.session().planTargetAt(1).orElseThrow().planId());
    }

    @Test
    void deletePlanGalleryTrip_confirmed_removesSelectedPlanAndRefreshesTargets() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2), LocalTime.of(9, 0));
        Plan secondPlan = service.addPlan(trip.id(), "Osaka", LocalDate.of(2027, 1, 3),
                LocalTime.of(9, 0));
        CliContext context = createContext(service, "yes\n");
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new DeletePlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertTrue(result.message().contains("Plan deleted."));
        assertEquals(List.of(secondPlan), service.getTrip(trip.id()).orElseThrow().plans());
        assertEquals(secondPlan.id(), context.session().planTargetAt(1).orElseThrow().planId());
    }

    @Test
    void deletePlanGalleryTrip_sequentialDeletion_usesRefreshedNumbering() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2), LocalTime.of(9, 0));
        service.addPlan(trip.id(), "Osaka", LocalDate.of(2027, 1, 3), LocalTime.of(9, 0));
        CliContext context = createContext(service, "yes\nyes\n");
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());

        new DeletePlanCommand(1).execute(context);
        CommandResult result = new DeletePlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("Plan deleted."));
        assertTrue(service.getTrip(trip.id()).orElseThrow().plans().isEmpty());
        assertTrue(context.session().planTargetAt(1).isEmpty());
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
    }

    @Test
    void deletePlanGalleryTrip_cancelledOrUppercaseConfirmation_preservesPlanAndRefreshes() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan plan = service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));
        CliContext context = createContext(service, "YES\nno\n");
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new DeletePlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertTrue(result.message().contains("Plan deletion cancelled."));
        assertEquals(List.of(plan), service.getTrip(trip.id()).orElseThrow().plans());
        assertEquals(plan.id(), context.session().planTargetAt(1).orElseThrow().planId());
    }

    @Test
    void deletePlanGalleryTrip_missingOrReclassifiedTrip_doesNotPromptAndReturnsToOwner() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip missingTrip = service.createTrip("Missing", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Trip reclassifiedTrip = service.createTrip("Reclassified", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.addPlan(missingTrip.id(), "Tokyo", LocalDate.of(2027, 1, 2), LocalTime.of(9, 0));
        service.addPlan(reclassifiedTrip.id(), "Osaka", LocalDate.of(2027, 1, 2), LocalTime.of(9, 0));

        CliContext missingContext = createNoPromptContext(service);
        missingContext.session().enterGalleryTrip(missingTrip.id());
        missingContext.selectedGalleryTripView(service.getTrip(missingTrip.id()).orElseThrow());
        repository.delete(missingTrip.id());
        CommandResult missingResult = new DeletePlanCommand(1).execute(missingContext);

        CliContext reclassifiedContext = createNoPromptContext(service);
        reclassifiedContext.session().enterGalleryTrip(reclassifiedTrip.id());
        reclassifiedContext.selectedGalleryTripView(
                service.getTrip(reclassifiedTrip.id()).orElseThrow());
        repository.save(new Trip(reclassifiedTrip.id(), reclassifiedTrip.title(),
                LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5)));
        CommandResult reclassifiedResult = new DeletePlanCommand(1).execute(reclassifiedContext);

        assertTrue(missingResult.message().contains("Selected Trip could not be found."));
        assertEquals(CliMode.GALLERY, missingContext.session().mode());
        assertTrue(reclassifiedResult.message().contains("Selected Trip is no longer available."));
        assertEquals(CliMode.ORGANISE, reclassifiedContext.session().mode());
    }

    @Test
    void deletePlanGalleryTrip_mismatchedOrRemovedTarget_doesNotPrompt() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip selectedTrip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Trip otherTrip = service.createTrip("Korea", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan plan = service.addPlan(selectedTrip.id(), "Tokyo", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));
        CliContext mismatchContext = createNoPromptContext(service);
        mismatchContext.session().enterGalleryTrip(selectedTrip.id());
        mismatchContext.selectedGalleryTripView(selectedTrip);
        mismatchContext.session().setDisplayedPlanTargets(
                List.of(new PlanTarget(otherTrip.id(), plan.id())));
        CommandResult mismatchResult = new DeletePlanCommand(1).execute(mismatchContext);

        CliContext removedContext = createNoPromptContext(service);
        removedContext.session().enterGalleryTrip(selectedTrip.id());
        removedContext.selectedGalleryTripView(service.getTrip(selectedTrip.id()).orElseThrow());
        assertEquals(List.of(plan), service.getTrip(selectedTrip.id()).orElseThrow().plans());
        repository.save(selectedTrip);
        CommandResult removedResult = new DeletePlanCommand(1).execute(removedContext);

        assertTrue(mismatchResult.message().contains("The selected Plan is no longer available."));
        assertTrue(removedResult.message().contains("The selected Plan is no longer available."));
    }

    @Test
    void deletePlanGalleryTrip_removedAfterConfirmation_failsSafelyAndKeepsOtherPlan() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.addPlan(trip.id(), "Tokyo", LocalDate.of(2027, 1, 2), LocalTime.of(9, 0));
        Plan retainedPlan = service.addPlan(trip.id(), "Osaka", LocalDate.of(2027, 1, 3),
                LocalTime.of(9, 0));
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        BufferedReader input = new BufferedReader(new StringReader("yes\n")) {
            @Override
            public String readLine() throws IOException {
                String line = super.readLine();
                repository.save(new Trip(trip.id(), trip.title(), trip.startDate(), trip.endDate())
                        .withAddedPlan(retainedPlan));
                return line;
            }
        };
        CliContext context = new CliContext(service, new CliSession(),
                new CliPrompter(input, writer), new CliFormatter(), writer);
        context.session().enterGalleryTrip(trip.id());
        context.selectedGalleryTripView(service.getTrip(trip.id()).orElseThrow());

        CommandResult result = new DeletePlanCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("Plan not found."));
        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertEquals(List.of(retainedPlan), service.getTrip(trip.id()).orElseThrow().plans());
        assertEquals(retainedPlan.id(), context.session().planTargetAt(1).orElseThrow().planId());
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
