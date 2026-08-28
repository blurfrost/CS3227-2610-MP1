package doggo.ui.cli;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a Plan together with its owning Trip.
 *
 * @param tripId Owning Trip identity.
 * @param planId Plan identity.
 */
record PlanTarget(UUID tripId, UUID planId) {
    /**
     * Creates a Plan target with non-null identities.
     */
    PlanTarget {
        Objects.requireNonNull(tripId);
        Objects.requireNonNull(planId);
    }
}
