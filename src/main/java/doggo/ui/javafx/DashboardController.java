package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import doggo.application.DashboardEntry;
import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Trip;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

/**
 * Displays today's Plans and the details of the selected Dashboard entry.
 */
public final class DashboardController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
    private static final DateTimeFormatter DETAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Application service used to query Dashboard entries.
     */
    private final DoggoService service;

    /**
     * Dashboard date label.
     */
    @FXML
    private Label dateLabel;

    /**
     * Dashboard Plan list.
     */
    @FXML
    private ListView<DashboardEntry> planList;

    /**
     * Empty-state message for the Dashboard list.
     */
    @FXML
    private Label emptyState;

    /**
     * Empty-state message for the detail pane.
     */
    @FXML
    private Label detailEmptyState;

    /**
     * Selected Plan detail content.
     */
    @FXML
    private VBox detailContent;

    /**
     * Selected Plan destination label.
     */
    @FXML
    private TextFlow detailDestinationLabel;

    /**
     * Selected Plan Trip label.
     */
    @FXML
    private TextFlow detailTripLabel;

    /**
     * Selected Plan schedule label.
     */
    @FXML
    private Label detailScheduleLabel;

    /**
     * Button for editing the selected Plan.
     */
    @FXML
    private Button editPlanButton;

    /**
     * Button for deleting the selected Plan.
     */
    @FXML
    private Button deletePlanButton;

    /**
     * Selected Plan review label.
     */
    @FXML
    private Label detailReviewLabel;

    /**
     * Creates a Dashboard controller backed by the specified application service.
     *
     * @param service Application service used to query Dashboard entries.
     */
    public DashboardController(DoggoService service) {
        this.service = Objects.requireNonNull(service);
    }

    /**
     * Loads today's Dashboard entries and initializes selection handling.
     */
    @FXML
    private void initialize() {
        planList.setCellFactory(list -> new DashboardEntryCell());
        planList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previousEntry, selectedEntry) -> showDetails(selectedEntry));
        refresh();
    }

    /**
     * Refreshes the Dashboard snapshot from the application service.
     */
    void refresh() {
        DashboardEntry selectedEntry = planList.getSelectionModel().getSelectedItem();
        refreshAndSelect(selectedEntry == null ? null : selectedEntry.tripId(),
                selectedEntry == null ? null : selectedEntry.plan().id());
    }

    /**
     * Refreshes the Dashboard and selects the specified Plan when it remains visible.
     * If the Plan is no longer scheduled today, the first visible Plan is selected.
     *
     * @param selectedTripId Owning Trip identity to select, or null for the first Plan.
     * @param selectedPlanId Plan identity to select, or null for the first Plan.
     */
    void refreshAndSelect(UUID selectedTripId, UUID selectedPlanId) {
        try {
            List<DashboardEntry> entries = service.getDashboardEntries();
            dateLabel.setText(entries.isEmpty()
                    ? "No plans scheduled for today"
                    : entries.getFirst().plan().date().format(DATE_FORMATTER));
            planList.getItems().setAll(entries);
            boolean isEmpty = entries.isEmpty();
            emptyState.setVisible(isEmpty);
            emptyState.setManaged(isEmpty);
            if (isEmpty) {
                showDetails(null);
            } else {
                int selectedIndex = findEntryIndex(entries, selectedTripId, selectedPlanId);
                planList.getSelectionModel().select(selectedIndex < 0 ? 0 : selectedIndex);
            }
        } catch (RepositoryException exception) {
            dateLabel.setText("Today's itinerary is unavailable");
            planList.getItems().clear();
            emptyState.setText("We could not load today's plans. Please try again.");
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            showDetails(null);
            showLoadError();
        }
    }

    /**
     * Finds a Dashboard entry by its owning Trip and Plan identities.
     *
     * @param entries Dashboard entries to search.
     * @param tripId Owning Trip identity to find.
     * @param planId Plan identity to find.
     * @return Matching index, or -1 when no identity pair is supplied or found.
     */
    private static int findEntryIndex(List<DashboardEntry> entries, UUID tripId, UUID planId) {
        if (tripId == null || planId == null) {
            return -1;
        }
        for (int index = 0; index < entries.size(); index++) {
            DashboardEntry entry = entries.get(index);
            if (entry.tripId().equals(tripId) && entry.plan().id().equals(planId)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Updates the detail pane for the selected Dashboard entry.
     *
     * @param entry Selected Dashboard entry, or null when nothing is selected.
     */
    private void showDetails(DashboardEntry entry) {
        boolean hasSelection = entry != null;
        detailEmptyState.setVisible(!hasSelection);
        detailEmptyState.setManaged(!hasSelection);
        detailContent.setVisible(hasSelection);
        detailContent.setManaged(hasSelection);
        editPlanButton.setDisable(!hasSelection);
        deletePlanButton.setDisable(!hasSelection);
        if (!hasSelection) {
            return;
        }

        Plan plan = entry.plan();
        DetailTextSupport.setText(detailDestinationLabel, plan.destination(), "detail-destination-text");
        DetailTextSupport.setLabeledText(detailTripLabel, "From ", entry.tripTitle(),
                "detail-trip-text", "detail-trip-name-text");
        detailScheduleLabel.setText(plan.date().format(DETAIL_DATE_FORMATTER)
                + " at " + plan.time().format(TIME_FORMATTER));
        detailReviewLabel.setText(ReviewDisplaySupport.format(plan.review()));
    }

    /**
     * Opens the modal form for editing the selected Dashboard Plan.
     */
    @FXML
    private void handleEditPlan() {
        DashboardEntry selectedEntry = planList.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            return;
        }
        try {
            Trip trip = service.getTrip(selectedEntry.tripId())
                    .orElseThrow(() -> new IllegalArgumentException("Trip not found."));
            PlanCreationDialog dialog = new PlanCreationDialog(service, trip, selectedEntry.plan(),
                    editPlanButton.getScene().getWindow());
            dialog.showAndWait().ifPresent(updatedPlan -> refreshAndSelect(selectedEntry.tripId(), updatedPlan.id()));
        } catch (RepositoryException exception) {
            showEditError("The Plan could not be loaded. Check the database and try again.");
        } catch (IllegalArgumentException exception) {
            showEditError(exception.getMessage());
        }
    }

    /**
     * Opens a confirmation dialog and deletes the selected Dashboard Plan.
     */
    @FXML
    private void handleDeletePlan() {
        DashboardEntry selectedEntry = planList.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            return;
        }
        DashboardEntry replacementEntry = findAdjacentEntry(
                planList.getItems(), planList.getSelectionModel().getSelectedIndex());
        if (!DeletionConfirmationDialog.confirmPlan(selectedEntry.plan(), selectedEntry.tripTitle(),
                deletePlanButton.getScene().getWindow())) {
            return;
        }
        try {
            service.deletePlan(selectedEntry.tripId(), selectedEntry.plan().id());
            if (replacementEntry == null) {
                refresh();
            } else {
                refreshAndSelect(replacementEntry.tripId(), replacementEntry.plan().id());
            }
        } catch (RepositoryException | IllegalArgumentException exception) {
            refresh();
            showDeleteError(exception.getMessage());
        }
    }

    /**
     * Returns the next entry after the deleted row, or the previous entry when it was last.
     *
     * @param entries Dashboard entries containing the deleted row.
     * @param deletedIndex Index of the deleted row.
     * @return Replacement entry, or null when no entries remain.
     */
    private static DashboardEntry findAdjacentEntry(List<DashboardEntry> entries, int deletedIndex) {
        if (entries.size() <= 1) {
            return null;
        }
        return deletedIndex + 1 < entries.size()
                ? entries.get(deletedIndex + 1)
                : entries.get(deletedIndex - 1);
    }

    /**
     * Shows an actionable error when Dashboard data cannot be loaded.
     */
    private static void showLoadError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Dashboard unavailable");
        alert.setHeaderText("Today's plans could not be loaded.");
        alert.setContentText("Check that the database is accessible, then try again.");
        alert.showAndWait();
    }

    /**
     * Shows an actionable error when a Dashboard Plan cannot be edited.
     *
     * @param message Error message to display.
     */
    private static void showEditError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Plan unavailable");
        alert.setHeaderText("The selected Plan could not be edited.");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an actionable error when a Dashboard Plan cannot be deleted.
     *
     * @param message Error message to display.
     */
    private static void showDeleteError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Plan unavailable");
        alert.setHeaderText("The selected Plan could not be deleted.");
        alert.setContentText(message == null ? "Check that the database is accessible, then try again." : message);
        alert.showAndWait();
    }
}
