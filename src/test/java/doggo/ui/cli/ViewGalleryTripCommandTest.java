package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.domain.Trip;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class ViewGalleryTripCommandTest {
    @Test
    void execute_repositoryOrderChangesAfterDisplay_selectsDisplayedPastTrip() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        service.createTrip("Japan", LocalDate.of(2027, 1, 3), LocalDate.of(2027, 1, 4));
        service.createTrip("Korea", LocalDate.of(2027, 1, 4), LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service);

        context.galleryMenu();
        service.createTrip("Earlier", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2));

        CommandResult result = new ViewGalleryTripCommand(1).execute(context);

        assertEquals(CliMode.GALLERY_TRIP, context.session().mode());
        assertTrue(result.message().contains("Viewing past Trip: Japan"));
        assertFalse(result.message().contains("Viewing past Trip: Earlier"));
    }

    @Test
    void execute_tripNoLongerPast_reportsStaleTargetAndRefreshesGallery() {
        InMemoryTripRepository repository = new InMemoryTripRepository();
        DoggoService service = new DoggoService(repository, TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        CliContext context = createContext(service);
        context.session().enterGallery();
        context.galleryMenu();
        repository.save(new Trip(trip.id(), trip.title(), LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 6)));

        CommandResult result = new ViewGalleryTripCommand(1).execute(context);

        assertEquals(CliMode.GALLERY, context.session().mode());
        assertTrue(result.message().contains(
                "The selected Trip is no longer available in Gallery."));
        assertTrue(result.message().contains("There are no past Trips."));
    }

    private static CliContext createContext(DoggoService service) {
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        return new CliContext(service, new CliSession(),
                new CliPrompter(new BufferedReader(new StringReader("")), writer),
                new CliFormatter(), writer);
    }
}
