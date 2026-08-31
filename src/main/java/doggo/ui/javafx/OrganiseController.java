package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Trip;
import doggo.domain.TripStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

/**
 * Displays current and future Trips with the selected Trip's itinerary.
 */
public final class OrganiseController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");

    /**
     * Application service used to query Trips and Plans.
     */
    private final DoggoService service;

    /**
     * Handler invoked after a Trip is edited, allowing the shell to route it by status.
     */
    private Consumer<Trip> tripEditedHandler = trip -> refreshAndSelect(trip.id());

    /**
     * Button for adding a Plan to the selected Trip.
     */
    @FXML
    private Button addPlanButton;

    /**
     * Button for editing the selected Trip.
     */
    @FXML
    private Button editTripButton;

    /**
     * Button for adding or editing the selected Trip's Review.
     */
    @FXML
    private Button reviewTripButton;

    /**
     * Button for deleting the selected Trip.
     */
    @FXML
    private Button deleteTripButton;

    /**
     * Current and future Trip list.
     */
    @FXML
    private ListView<Trip> tripList;

    /**
     * Empty-state message for the Trip list.
     */
    @FXML
    private Label emptyState;

    /**
     * Empty-state message for the detail pane.
     */
    @FXML
    private Label detailEmptyState;

    /**
     * Selected Trip detail content.
     */
    @FXML
    private VBox detailContent;

    /**
     * Selected Trip title label.
     */
    @FXML
    private TextFlow detailTitleLabel;

    /**
     * Selected Trip date range label.
     */
    @FXML
    private Label detailDatesLabel;

    /**
     * Selected Trip status label.
     */
    @FXML
    private Label statusLabel;

    /**
     * Selected Trip review label.
     */
    @FXML
    private Label detailReviewLabel;

    /**
     * Selected Trip Plan list.
     */
    @FXML
    private ListView<Plan> planList;

    /**
     * Empty-state message for the selected Trip's Plan list.
     */
    @FXML
    private Label planEmptyState;

    /**
     * Heading for the selected Trip's Plan list.
     */
    @FXML
    private Label plansLabel;

    /**
     * Creates an Organise controller backed by the specified application service.
     *
     * @param service Application service used to query Trips and Plans.
     */
    public OrganiseController(DoggoService service) {
        this.service = Objects.requireNonNull(service);
    }

    /**
     * Sets the handler invoked after a Trip is edited.
     *
     * @param tripEditedHandler Handler that receives the updated Trip.
     */
    void setTripEditedHandler(Consumer<Trip> tripEditedHandler) {
        this.tripEditedHandler = Objects.requireNonNull(tripEditedHandler);
    }

    /**
     * Loads current and future Trips and initializes selection handling.
     */
    @FXML
    private void initialize() {
        tripList.setCellFactory(list -> new TripCell());
        planList.setCellFactory(list -> new PlanCell(this::handleEditPlan, this::handleDeletePlan));
        tripList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previousTrip, selectedTrip) -> showDetails(selectedTrip));
        refresh();
    }

    /**
     * Refreshes the current Organise snapshot and preserves selection when possible.
     */
    void refresh() {
        Trip selectedTrip = tripList.getSelectionModel().getSelectedItem();
        refreshAndSelect(selectedTrip == null ? null : selectedTrip.id());
    }

    /**
     * Refreshes Organise and selects the Trip with the specified identity.
     * If the identity is absent, the first current or future Trip is selected.
     *
     * @param selectedTripId Trip identity to select, or null to select the first Trip.
     */
    void refreshAndSelect(UUID selectedTripId) {
        refreshAndSelect(selectedTripId, null);
    }

    /**
     * Refreshes Organise and selects the specified Trip and Plan.
     * If either identity is absent, the first available item is selected where appropriate.
     *
     * @param selectedTripId Trip identity to select, or null to select the first Trip.
     * @param selectedPlanId Plan identity to select, or null to clear Plan selection.
     */
    void refreshAndSelect(UUID selectedTripId, UUID selectedPlanId) {
        try {
            List<Trip> trips = service.getCurrentAndFutureTrips();
            tripList.getItems().setAll(trips);
            boolean isEmpty = trips.isEmpty();
            emptyState.setVisible(isEmpty);
            emptyState.setManaged(isEmpty);
            if (isEmpty) {
                showDetails(null);
            } else {
                selectTrip(trips, selectedTripId, selectedPlanId);
            }
        } catch (RepositoryException exception) {
            tripList.getItems().clear();
            emptyState.setText("We could not load your trips. Please try again.");
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            showDetails(null);
            showLoadError();
        }
    }

    /**
     * Selects the requested Trip or falls back to the first available Trip.
     *
     * @param trips Trips currently displayed by Organise.
     * @param selectedTripId Trip identity to select, or null for the first Trip.
     * @param selectedPlanId Plan identity to select, or null to clear Plan selection.
     */
    private void selectTrip(List<Trip> trips, UUID selectedTripId, UUID selectedPlanId) {
        int selectedIndex = selectedTripId == null
                ? 0
                : findTripIndex(trips, selectedTripId);
        selectedIndex = selectedIndex < 0 ? 0 : selectedIndex;
        tripList.getSelectionModel().select(selectedIndex);
        if (selectedPlanId != null) {
            showDetails(trips.get(selectedIndex), selectedPlanId);
        }
    }

    /**
     * Finds the list index of a Trip identity.
     *
     * @param trips Trips to search.
     * @param tripId Trip identity to find.
     * @return Matching index, or -1 when absent.
     */
    private static int findTripIndex(List<Trip> trips, UUID tripId) {
        for (int index = 0; index < trips.size(); index++) {
            if (trips.get(index).id().equals(tripId)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Opens the modal form for adding a Plan to the selected Trip.
     */
    @FXML
    private void handleAddPlan() {
        Trip selectedTrip = tripList.getSelectionModel().getSelectedItem();
        if (selectedTrip == null) {
            return;
        }
        PlanCreationDialog dialog = new PlanCreationDialog(service, selectedTrip,
                addPlanButton.getScene().getWindow());
        dialog.showAndWait().ifPresent(plan -> refreshAndSelect(selectedTrip.id(), plan.id()));
    }

    /**
     * Opens the modal form for editing a Plan in the selected Trip.
     *
     * @param plan Plan selected for editing.
     */
    private void handleEditPlan(Plan plan) {
        Trip selectedTrip = tripList.getSelectionModel().getSelectedItem();
        if (selectedTrip == null) {
            return;
        }
        PlanCreationDialog dialog = new PlanCreationDialog(service, selectedTrip, plan,
                addPlanButton.getScene().getWindow());
        dialog.showAndWait().ifPresent(updatedPlan -> refreshAndSelect(selectedTrip.id(), updatedPlan.id()));
    }

    /**
     * Opens a confirmation dialog and deletes a Plan from the selected Trip.
     *
     * @param plan Plan selected for deletion.
     */
    private void handleDeletePlan(Plan plan) {
        Trip selectedTrip = tripList.getSelectionModel().getSelectedItem();
        int deletedIndex = planList.getItems().indexOf(plan);
        if (selectedTrip == null || deletedIndex < 0) {
            return;
        }
        Plan replacementPlan = findAdjacentPlan(planList.getItems(), deletedIndex);
        if (!DeletionConfirmationDialog.confirmPlan(plan, selectedTrip.title(),
                addPlanButton.getScene().getWindow())) {
            return;
        }
        try {
            service.deletePlan(selectedTrip.id(), plan.id());
            refreshAndSelect(selectedTrip.id(), replacementPlan == null ? null : replacementPlan.id());
        } catch (RepositoryException | IllegalArgumentException exception) {
            refreshAndSelect(selectedTrip.id());
            showDeleteError(exception.getMessage());
        }
    }

    /**
     * Opens a confirmation dialog and deletes the selected Trip.
     */
    @FXML
    private void handleDeleteTrip() {
        Trip selectedTrip = tripList.getSelectionModel().getSelectedItem();
        int deletedIndex = tripList.getItems().indexOf(selectedTrip);
        if (selectedTrip == null || deletedIndex < 0) {
            return;
        }
        Trip replacementTrip = findAdjacentTrip(tripList.getItems(), deletedIndex);
        if (!DeletionConfirmationDialog.confirmTrip(selectedTrip,
                deleteTripButton.getScene().getWindow())) {
            return;
        }
        try {
            service.deleteTrip(selectedTrip.id());
            refreshAndSelect(replacementTrip == null ? null : replacementTrip.id());
        } catch (RepositoryException | IllegalArgumentException exception) {
            refreshAndSelect(selectedTrip.id());
            showTripDeleteError(exception.getMessage());
        }
    }

    /**
     * Returns the next Plan after the deleted row, or the previous Plan when it was last.
     *
     * @param plans Plans containing the deleted row.
     * @param deletedIndex Index of the deleted row.
     * @return Replacement Plan, or null when no Plans remain.
     */
    private static Plan findAdjacentPlan(List<Plan> plans, int deletedIndex) {
        if (plans.size() <= 1) {
            return null;
        }
        return deletedIndex + 1 < plans.size()
                ? plans.get(deletedIndex + 1)
                : plans.get(deletedIndex - 1);
    }

    /**
     * Updates the detail pane for the selected Trip.
     *
     * @param trip Selected Trip, or null when nothing is selected.
     */
    private void showDetails(Trip trip) {
        showDetails(trip, null);
    }

    /**
     * Updates the detail pane for the selected Trip and optionally selects a Plan.
     *
     * @param trip Selected Trip, or null when nothing is selected.
     * @param selectedPlanId Plan identity to select, or null to clear Plan selection.
     */
    private void showDetails(Trip trip, UUID selectedPlanId) {
        boolean hasSelection = trip != null;
        detailEmptyState.setVisible(!hasSelection);
        detailEmptyState.setManaged(!hasSelection);
        detailContent.setVisible(hasSelection);
        detailContent.setManaged(hasSelection);
        statusLabel.setVisible(hasSelection);
        statusLabel.setManaged(hasSelection);
        editTripButton.setDisable(!hasSelection);
        reviewTripButton.setDisable(!hasSelection);
        deleteTripButton.setDisable(!hasSelection);
        addPlanButton.setDisable(!hasSelection);
        if (!hasSelection) {
            return;
        }

        TripStatus status = service.getTripStatus(trip);
        List<Plan> plans = service.getPlans(trip);
        DetailTextSupport.setText(detailTitleLabel, trip.title(), "detail-destination-text");
        detailDatesLabel.setText(formatDateRange(trip));
        plansLabel.setText("Plans (" + plans.size() + ")");
        reviewTripButton.setText(trip.review().isPresent() ? "Edit Review" : "Add Review");
        detailReviewLabel.setText(ReviewDisplaySupport.format(trip.review()));
        statusLabel.setText(formatStatus(status));
        statusLabel.getStyleClass().removeAll("status-current", "status-future");
        statusLabel.getStyleClass().add(status == TripStatus.CURRENT
                ? "status-current"
                : "status-future");
        planList.getItems().setAll(plans);
        if (selectedPlanId == null) {
            planList.getSelectionModel().clearSelection();
        } else {
            selectPlan(plans, selectedPlanId);
        }
        boolean isPlanEmpty = plans.isEmpty();
        planEmptyState.setVisible(isPlanEmpty);
        planEmptyState.setManaged(isPlanEmpty);
    }

    /**
     * Opens the modal form for editing the selected Trip.
     */
    @FXML
    private void handleEditTrip() {
        Trip selectedTrip = tripList.getSelectionModel().getSelectedItem();
        if (selectedTrip == null) {
            return;
        }
        TripCreationDialog dialog = new TripCreationDialog(service, selectedTrip,
                editTripButton.getScene().getWindow());
        dialog.showAndWait().ifPresent(tripEditedHandler);
    }

    /**
     * Opens the modal form for adding, editing, or removing the selected Trip's Review.
     */
    @FXML
    private void handleReviewTrip() {
        Trip selectedTrip = tripList.getSelectionModel().getSelectedItem();
        if (selectedTrip == null) {
            return;
        }
        ReviewDialog<Trip> dialog = ReviewDialog.forTrip(service, selectedTrip,
                reviewTripButton.getScene().getWindow());
        dialog.showAndWait().ifPresent(updatedTrip -> refreshAndSelect(updatedTrip.id()));
    }

    /**
     * Selects the requested Plan or clears selection when it is absent.
     *
     * @param plans Plans currently displayed for the selected Trip.
     * @param selectedPlanId Plan identity to select.
     */
    private void selectPlan(List<Plan> plans, UUID selectedPlanId) {
        int selectedIndex = findPlanIndex(plans, selectedPlanId);
        if (selectedIndex < 0) {
            planList.getSelectionModel().clearSelection();
            return;
        }
        planList.getSelectionModel().select(selectedIndex);
    }

    /**
     * Finds the list index of a Plan identity.
     *
     * @param plans Plans to search.
     * @param planId Plan identity to find.
     * @return Matching index, or -1 when absent.
     */
    private static int findPlanIndex(List<Plan> plans, UUID planId) {
        for (int index = 0; index < plans.size(); index++) {
            if (plans.get(index).id().equals(planId)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Returns the next Trip after the deleted row, or the previous Trip when it was last.
     *
     * @param trips Trips containing the deleted row.
     * @param deletedIndex Index of the deleted row.
     * @return Replacement Trip, or null when no Trips remain.
     */
    private static Trip findAdjacentTrip(List<Trip> trips, int deletedIndex) {
        if (trips.size() <= 1) {
            return null;
        }
        return deletedIndex + 1 < trips.size()
                ? trips.get(deletedIndex + 1)
                : trips.get(deletedIndex - 1);
    }

    /**
     * Formats the selected Trip's inclusive date range.
     *
     * @param trip Trip whose dates are formatted.
     * @return Human-readable date range.
     */
    private static String formatDateRange(Trip trip) {
        return DATE_FORMATTER.format(trip.startDate()) + " – " + DATE_FORMATTER.format(trip.endDate());
    }

    /**
     * Formats a Trip status for the status badge.
     *
     * @param status Trip status to format.
     * @return Uppercase status text.
     */
    private static String formatStatus(TripStatus status) {
        return status == TripStatus.CURRENT ? "IN PROGRESS" : "UPCOMING";
    }

    /**
     * Shows an actionable error when Trip data cannot be loaded.
     */
    private static void showLoadError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Organise unavailable");
        alert.setHeaderText("Your trips could not be loaded.");
        alert.setContentText("Check that the database is accessible, then try again.");
        alert.showAndWait();
    }

    /**
     * Shows an actionable error when a Plan cannot be deleted.
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

    /**
     * Shows an actionable error when a Trip cannot be deleted.
     *
     * @param message Error message to display.
     */
    private static void showTripDeleteError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Trip unavailable");
        alert.setHeaderText("The selected Trip could not be deleted.");
        alert.setContentText(message == null ? "Check that the database is accessible, then try again." : message);
        alert.showAndWait();
    }
}
