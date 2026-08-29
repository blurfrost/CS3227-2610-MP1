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
import java.util.Optional;
import java.util.OptionalInt;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.domain.Review;
import doggo.domain.Trip;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class GalleryReviewTest {
    @Test
    void reviewGalleryTrip_addsReviewAndRefreshesGallery() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "5\n  Wonderful trip.  \n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("Review added."));
        assertTrue(result.message().contains("   Rating: 5/5\n"));
        assertTrue(result.message().contains("   Review: Wonderful trip.\n"));
        assertEquals(Optional.of(new Review(OptionalInt.of(5), Optional.of("Wonderful trip."))),
                service.getTrip(trip.id()).orElseThrow().review());
    }

    @Test
    void reviewGalleryTrip_editPreservesBlankFieldAndClearsWithDash() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.setTripReview(trip.id(), new Review(OptionalInt.of(4), Optional.of("Good trip.")));
        CliContext context = createContext(service, "\nGreat trip.\n-\n-\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult updateResult = new ReviewTripCommand(1).execute(context);
        CommandResult removeResult = new ReviewTripCommand(1).execute(context);

        assertTrue(updateResult.message().contains("Review updated."));
        assertTrue(removeResult.message().contains("Review removed."));
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().review());
    }

    @Test
    void reviewGalleryTrip_equalStateReportsNoChanges() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.setTripReview(trip.id(), new Review(OptionalInt.of(4), Optional.of("Good trip.")));
        CliContext context = createContext(service, "\n\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertTrue(result.message().contains("No changes made."));
        assertEquals(Optional.of(new Review(OptionalInt.of(4), Optional.of("Good trip."))),
                service.getTrip(trip.id()).orElseThrow().review());
    }

    @Test
    void reviewGalleryTrip_newReviewWithAbsentFieldsReportsNoChanges() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "-\n-\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertTrue(result.message().contains("No changes made."));
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().review());
    }

    @Test
    void reviewGalleryTrip_invalidRatingsRepromptUntilValid() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "0\n6\nabc\n5\nGreat\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertTrue(result.message().contains("Review added."));
        assertTrue(contextOutput(context).contains("Rating must be a whole number from 1 to 5."));
        assertEquals(Optional.of(new Review(OptionalInt.of(5), Optional.of("Great"))),
                service.getTrip(trip.id()).orElseThrow().review());
    }

    @Test
    void reviewGalleryTrip_eofAtEitherPromptDoesNotMutate() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext ratingEofContext = createContext(service, "");
        ratingEofContext.session().enterGallery();
        ratingEofContext.galleryMenu();

        CommandResult ratingEofResult = new ReviewTripCommand(1).execute(ratingEofContext);

        assertTrue(ratingEofResult.shouldExit());
        assertEquals("Bye!", ratingEofResult.message());
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().review());

        CliContext textEofContext = createContext(service, "5\n");
        textEofContext.session().enterGallery();
        textEofContext.galleryMenu();

        CommandResult textEofResult = new ReviewTripCommand(1).execute(textEofContext);

        assertTrue(textEofResult.shouldExit());
        assertEquals("Bye!", textEofResult.message());
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().review());
    }

    @Test
    void reviewGalleryTrip_invalidIndexDoesNotPrompt() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "5\nShould not be consumed\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new ReviewTripCommand(0).execute(context);

        assertFalse(result.shouldExit());
        assertTrue(result.message().contains("Trip number should be 1."));
        assertFalse(contextOutput(context).contains("Enter rating"));
    }

    @Test
    void reviewGalleryTrip_deletedTargetBeforePromptDoesNotPrompt() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "5\nShould not be consumed\n");
        context.session().enterGallery();
        context.galleryMenu();
        repository.delete(trip.id());

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Trip is no longer available in Gallery."));
        assertFalse(contextOutput(context).contains("Enter rating"));
    }

    @Test
    void reviewGalleryTrip_reclassifiedTargetBeforePromptDoesNotPrompt() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service, "5\nShould not be consumed\n");
        context.session().enterGallery();
        context.galleryMenu();
        repository.save(new Trip(trip.id(), trip.title(), LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 6)));

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Trip is no longer available in Gallery."));
        assertFalse(contextOutput(context).contains("Enter rating"));
    }

    @Test
    void reviewGalleryTrip_deletedTargetAfterPromptDoesNotMutate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContextWithReader(service, new MutatingReader("5\nGreat\n", () ->
                repository.delete(trip.id())));
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Trip is no longer available in Gallery."));
        assertTrue(service.getTrip(trip.id()).isEmpty());
    }

    @Test
    void reviewGalleryTrip_reclassifiedTargetAfterPromptDoesNotMutate() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContextWithReader(service, new MutatingReader("5\nGreat\n", () ->
                repository.save(new Trip(trip.id(), trip.title(), LocalDate.of(2027, 1, 5),
                        LocalDate.of(2027, 1, 6)))));
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Trip is no longer available in Gallery."));
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().review());
        assertTrue(result.message().contains("There are no past Trips."));
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
