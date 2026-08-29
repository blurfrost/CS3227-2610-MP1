package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PlanTargetTest {
    @Test
    void createTarget_validIds_storesBothIds() {
        UUID tripId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        PlanTarget target = new PlanTarget(tripId, planId);

        assertEquals(tripId, target.tripId());
        assertEquals(planId, target.planId());
    }

    @Test
    void createTarget_nullId_throwsException() {
        UUID tripId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        assertThrows(NullPointerException.class, () -> new PlanTarget(null, planId));
        assertThrows(NullPointerException.class, () -> new PlanTarget(tripId, null));
    }
}
