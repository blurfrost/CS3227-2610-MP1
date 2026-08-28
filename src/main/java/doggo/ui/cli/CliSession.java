package doggo.ui.cli;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class CliSession {
    private CliMode mode = CliMode.MAIN;
    private UUID selectedTripId;
    private List<UUID> displayedTripIds = List.of();
    private List<UUID> displayedPlanIds = List.of();

    CliMode mode() {
        return mode;
    }

    void enterMain() {
        mode = CliMode.MAIN;
        selectedTripId = null;
        displayedTripIds = List.of();
        displayedPlanIds = List.of();
    }

    void enterOrganise() {
        mode = CliMode.ORGANISE;
        selectedTripId = null;
        displayedTripIds = List.of();
        displayedPlanIds = List.of();
    }

    void enterTrip(UUID tripId) {
        mode = CliMode.TRIP;
        selectedTripId = Objects.requireNonNull(tripId);
        displayedTripIds = List.of();
        displayedPlanIds = List.of();
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

    void setDisplayedPlanIds(List<UUID> displayedPlanIds) {
        this.displayedPlanIds = List.copyOf(displayedPlanIds);
    }

    Optional<UUID> planIdAt(int oneBasedIndex) {
        if (oneBasedIndex < 1 || oneBasedIndex > displayedPlanIds.size()) {
            return Optional.empty();
        }
        return Optional.of(displayedPlanIds.get(oneBasedIndex - 1));
    }

    int displayedPlanCount() {
        return displayedPlanIds.size();
    }
}
