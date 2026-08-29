package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalInt;

import doggo.application.DoggoService;
import doggo.domain.Review;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class ReviewTripCommandTest {
    @Test
    void execute_newReview_persistsReviewAndRefreshesGallery() {
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
}
