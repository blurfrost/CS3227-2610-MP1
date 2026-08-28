package doggo.ui.cli;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class CliSession {
    private CliMode mode = CliMode.MAIN;
    private UUID selectedTripId;
    private List<UUID> displayedTripIds = List.of();
    private List<PlanTarget> displayedPlanTargets = List.of();

    CliMode mode() {
        return mode;
    }

    void enterMain() {
        mode = CliMode.MAIN;
        selectedTripId = null;
        displayedTripIds = List.of();
        displayedPlanTargets = List.of();
    }

    void enterOrganise() {
        mode = CliMode.ORGANISE;
        selectedTripId = null;
        displayedTripIds = List.of();
        displayedPlanTargets = List.of();
    }

    void enterTrip(UUID tripId) {
        mode = CliMode.TRIP;
        selectedTripId = Objects.requireNonNull(tripId);
        displayedTripIds = List.of();
        displayedPlanTargets = List.of();
    }

    void enterDashboard() {
        mode = CliMode.DASHBOARD;
        selectedTripId = null;
        displayedTripIds = List.of();
        displayedPlanTargets = List.of();
    }

    Optional<UUID> selectedTripId() {
        return Optional.ofNullable(selectedTripId);
    }

    void setDisplayedTripIds(List<UUID> displayedTripIds) {
        this.displayedTripIds = List.copyOf(displayedTripIds);
    }

    Optional<UUID> tripIdAt(int oneBasedIndex) {
        if (oneBasedIndex < 1 || oneBasedIndex > displayedTripIds.size()) {
            return Optional.empty();
        }
        return Optional.of(displayedTripIds.get(oneBasedIndex - 1));
    }

    int displayedTripCount() {
        return displayedTripIds.size();
    }

    void setDisplayedPlanTargets(UUID tripId, List<UUID> planIds) {
        Objects.requireNonNull(tripId);
        this.displayedPlanTargets = planIds.stream()
                .map(planId -> new PlanTarget(tripId, planId))
                .toList();
    }

    Optional<PlanTarget> planTargetAt(int oneBasedIndex) {
        if (oneBasedIndex < 1 || oneBasedIndex > displayedPlanTargets.size()) {
            return Optional.empty();
        }
        return Optional.of(displayedPlanTargets.get(oneBasedIndex - 1));
    }

    int displayedPlanCount() {
        return displayedPlanTargets.size();
    }
}
