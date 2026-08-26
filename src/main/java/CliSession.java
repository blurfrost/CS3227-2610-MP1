import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class CliSession {
    private CliMode mode = CliMode.MAIN;
    private UUID selectedTripId;
    private List<UUID> displayedTripIds = List.of();

    CliMode mode() {
        return mode;
    }

    void setMode(CliMode mode) {
        this.mode = mode;
    }

    Optional<UUID> selectedTripId() {
        return Optional.ofNullable(selectedTripId);
    }

    void setSelectedTripId(UUID selectedTripId) {
        this.selectedTripId = Objects.requireNonNull(selectedTripId);
    }

    void clearSelectedTripId() {
        selectedTripId = null;
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
}
