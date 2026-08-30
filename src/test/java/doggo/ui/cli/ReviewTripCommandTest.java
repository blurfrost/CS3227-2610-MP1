package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalInt;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.domain.Review;
import doggo.domain.Trip;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class ReviewTripCommandTest {
    @Test
    void execute_galleryTripReview_persistsReviewAndRefreshesGallery() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = CommandTestHelper.context(service, "5\nWonderful trip.\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertEquals(Optional.of(new Review(OptionalInt.of(5),
                Optional.of("Wonderful trip."))), service.getTrip(trip.id()).orElseThrow().review());
        assertTrue(result.message().contains("Review added."));
        assertTrue(result.message().contains("Rating: 5/5"));
        assertFalse(result.shouldExit());
    }

    @Test
    void execute_existingReview_blankRating_updatesTextAndPreservesRating() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        service.setTripReview(trip.id(), new Review(OptionalInt.of(4), Optional.of("Good trip.")));
        CliContext context = CommandTestHelper.context(service, "\nGreat trip.\n");
        context.session().enterGallery();
        context.galleryMenu();

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertEquals(Optional.of(new Review(OptionalInt.of(4), Optional.of("Great trip."))),
                service.getTrip(trip.id()).orElseThrow().review());
        assertTrue(result.message().contains("Review updated."));
        assertFalse(result.shouldExit());
    }

    @Test
    void execute_organiseCurrentTrip_persistsReviewAndRefreshesOrganise() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 9));
        CliContext context = CommandTestHelper.context(service, "5\nUpcoming trip.\n");
        context.session().enterOrganise();
        context.organiseMenu();

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertEquals(Optional.of(new Review(OptionalInt.of(5), Optional.of("Upcoming trip."))),
                service.getTrip(trip.id()).orElseThrow().review());
        assertTrue(result.message().contains("Review added."));
        assertTrue(result.message().contains("Japan"));
        assertEquals(CliMode.ORGANISE, context.session().mode());
        assertFalse(result.shouldExit());
    }

    @Test
    void execute_organiseFutureTrip_updatesAndRemovesReview() {
        DoggoService service = CommandTestHelper.service();
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 6),
                LocalDate.of(2027, 1, 9));
        CliContext context = CommandTestHelper.context(service,
                "\nUpcoming trip.\n-\n-\n");
        context.session().enterOrganise();
        context.organiseMenu();

        CommandResult updateResult = new ReviewTripCommand(1).execute(context);
        CommandResult removeResult = new ReviewTripCommand(1).execute(context);

        assertTrue(updateResult.message().contains("Review added."));
        assertTrue(removeResult.message().contains("Review removed."));
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().review());
        assertEquals(CliMode.ORGANISE, context.session().mode());
    }

    @Test
    void execute_organiseReclassifiedTrip_rejectsStaleTargetBeforePrompt() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 9));
        CliContext context = CommandTestHelper.context(service, "5\nShould not be consumed\n");
        context.session().enterOrganise();
        context.organiseMenu();
        repository.save(new Trip(trip.id(), trip.title(), LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4)));

        CommandResult result = new ReviewTripCommand(1).execute(context);

        assertTrue(result.message().contains("The selected Trip is no longer available."));
        assertEquals(Optional.empty(), service.getTrip(trip.id()).orElseThrow().review());
        assertEquals(CliMode.ORGANISE, context.session().mode());
    }
}
