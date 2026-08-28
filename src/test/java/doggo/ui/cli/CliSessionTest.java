package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CliSessionTest {
    @Test
    void enterTrip_setsTripModeAndSelectedTrip() {
        CliSession session = new CliSession();
        UUID tripId = UUID.randomUUID();

        session.enterTrip(tripId);

        assertEquals(CliMode.TRIP, session.mode());
        assertEquals(tripId, session.selectedTripId().orElseThrow());
    }

    @Test
    void enterOrganise_clearsTripSelectionAndDisplayedTrips() {
        CliSession session = new CliSession();
        session.enterTrip(UUID.randomUUID());
        session.setDisplayedTripIds(List.of(UUID.randomUUID()));
        session.setDisplayedPlanTargets(UUID.randomUUID(), List.of(UUID.randomUUID()));

        session.enterOrganise();

        assertEquals(CliMode.ORGANISE, session.mode());
        assertFalse(session.selectedTripId().isPresent());
        assertFalse(session.tripIdAt(1).isPresent());
        assertFalse(session.planTargetAt(1).isPresent());
    }

    @Test
    void enterMain_clearsTripSelectionAndDisplayedTrips() {
        CliSession session = new CliSession();
        session.enterTrip(UUID.randomUUID());
        session.setDisplayedTripIds(List.of(UUID.randomUUID()));
        session.setDisplayedPlanTargets(UUID.randomUUID(), List.of(UUID.randomUUID()));

        session.enterMain();

        assertEquals(CliMode.MAIN, session.mode());
        assertFalse(session.selectedTripId().isPresent());
        assertFalse(session.tripIdAt(1).isPresent());
        assertFalse(session.planTargetAt(1).isPresent());
    }

    @Test
    void enterTrip_nullTripId_throwsException() {
        CliSession session = new CliSession();

        assertThrows(NullPointerException.class, () -> session.enterTrip(null));
    }

    @Test
    void enterTrip_clearsDisplayedPlans() {
        CliSession session = new CliSession();
        session.setDisplayedPlanTargets(UUID.randomUUID(), List.of(UUID.randomUUID()));

        session.enterTrip(UUID.randomUUID());

        assertFalse(session.planTargetAt(1).isPresent());
    }

    @Test
    void enterDashboard_setsDashboardModeAndClearsDisplayedState() {
        CliSession session = new CliSession();
        session.enterTrip(UUID.randomUUID());
        session.setDisplayedTripIds(List.of(UUID.randomUUID()));
        session.setDisplayedPlanTargets(UUID.randomUUID(), List.of(UUID.randomUUID()));

        session.enterDashboard();

        assertEquals(CliMode.DASHBOARD, session.mode());
        assertFalse(session.selectedTripId().isPresent());
        assertFalse(session.tripIdAt(1).isPresent());
        assertFalse(session.planTargetAt(1).isPresent());
    }

    @Test
    void enterGallery_setsGalleryModeAndClearsDisplayedState() {
        CliSession session = new CliSession();
        session.enterTrip(UUID.randomUUID());
        session.setDisplayedTripIds(List.of(UUID.randomUUID()));
        session.setDisplayedPlanTargets(UUID.randomUUID(), List.of(UUID.randomUUID()));

        session.enterGallery();

        assertEquals(CliMode.GALLERY, session.mode());
        assertFalse(session.selectedTripId().isPresent());
        assertFalse(session.tripIdAt(1).isPresent());
        assertFalse(session.planTargetAt(1).isPresent());
    }

    @Test
    void enterGalleryTrip_setsReadOnlyTripModeAndSelection() {
        CliSession session = new CliSession();
        UUID tripId = UUID.randomUUID();

        session.enterGalleryTrip(tripId);

        assertEquals(CliMode.GALLERY_TRIP, session.mode());
        assertEquals(tripId, session.selectedTripId().orElseThrow());
    }

    @Test
    void planTargetAt_returnsDisplayedTargetByOneBasedIndex() {
        CliSession session = new CliSession();
        UUID tripId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        session.setDisplayedPlanTargets(tripId, List.of(planId));

        assertEquals(new PlanTarget(tripId, planId), session.planTargetAt(1).orElseThrow());
    }

    @Test
    void setDisplayedPlanTargets_copiesInputList() {
        CliSession session = new CliSession();
        UUID tripId = UUID.randomUUID();
        ArrayList<UUID> planIds = new ArrayList<>();
        UUID planId = UUID.randomUUID();
        planIds.add(planId);

        session.setDisplayedPlanTargets(tripId, planIds);
        planIds.clear();

        assertEquals(new PlanTarget(tripId, planId), session.planTargetAt(1).orElseThrow());
    }
}
