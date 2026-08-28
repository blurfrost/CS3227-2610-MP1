package doggo.application;

import java.util.Objects;
import java.util.UUID;

import doggo.domain.Plan;

/**
 * Represents a Plan with the Trip context required by the Dashboard.
 *
 * @param tripId Owning Trip identity.
 * @param tripTitle Owning Trip title.
 * @param plan Plan displayed by the Dashboard.
 */
public record DashboardEntry(UUID tripId, String tripTitle, Plan plan) {
    /**
     * Creates a Dashboard entry with the specified Trip context and Plan.
     */
    public DashboardEntry {
        tripId = Objects.requireNonNull(tripId);
        tripTitle = Objects.requireNonNull(tripTitle);
        if (tripTitle.isBlank()) {
            throw new IllegalArgumentException("Trip title cannot be blank.");
        }
        tripTitle = tripTitle.trim();
        plan = Objects.requireNonNull(plan);
    }
}
