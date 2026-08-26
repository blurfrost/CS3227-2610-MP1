import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class CliSession {
    private CliMode mode = CliMode.MAIN;
    private UUID selectedTripId;

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
}
