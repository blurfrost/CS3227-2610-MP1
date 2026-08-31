package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

/**
 * Displays completed Trips with their reviews and itineraries.
 */
public final class GalleryController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");

    /**
     * Application service used to query completed Trips and their Plans.
     */
    private final DoggoService service;

    /**
     * Button for adding a Plan to the selected Trip.
     */
    @FXML
    private Button addPlanButton;

    /**
     * Completed Trip list.
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
     * Selected Trip summary label.
     */
    @FXML
    private Label detailSummaryLabel;

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
     * Creates a Gallery controller backed by the specified application service.
     *
     * @param service Application service used to query completed Trips.
     */
    public GalleryController(DoggoService service) {
        this.service = Objects.requireNonNull(service);
    }

    /**
     * Loads completed Trips and initializes selection handling.
     */
    @FXML
    private void initialize() {
        tripList.setCellFactory(list -> new TripCell());
        planList.setCellFactory(list -> new PlanCell(this::handleEditPlan));
        tripList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previousTrip, selectedTrip) -> showDetails(selectedTrip));
        refresh();
    }

    /**
     * Refreshes the Gallery and preserves the current selection when possible.
     */
    void refresh() {
        Trip selectedTrip = tripList.getSelectionModel().getSelectedItem();
        refreshAndSelect(selectedTrip == null ? null : selectedTrip.id());
    }

    /**
     * Refreshes the Gallery and selects the Trip with the specified identity.
     * If the identity is absent, the first completed Trip is selected.
     *
     * @param selectedTripId Trip identity to select, or null to select the first Trip.
     */
    void refreshAndSelect(UUID selectedTripId) {
        refreshAndSelect(selectedTripId, null);
    }

    /**
     * Refreshes the Gallery and selects the specified Trip and Plan.
     * If either identity is absent, the first available item is selected where appropriate.
     *
     * @param selectedTripId Trip identity to select, or null to select the first Trip.
     * @param selectedPlanId Plan identity to select, or null to clear Plan selection.
     */
    void refreshAndSelect(UUID selectedTripId, UUID selectedPlanId) {
        try {
            List<Trip> trips = service.getPastTrips();
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
            emptyState.setText("We could not load your past trips. Please try again.");
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            showDetails(null);
            showLoadError();
        }
    }

    /**
     * Selects the requested Trip or falls back to the first available Trip.
     *
     * @param trips Trips currently displayed by the Gallery.
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
        addPlanButton.setDisable(!hasSelection);
        if (!hasSelection) {
            return;
        }

        List<Plan> plans = service.getPlans(trip);
        DetailTextSupport.setText(detailTitleLabel, trip.title(), "detail-destination-text");
        detailDatesLabel.setText(formatDateRange(trip));
        detailSummaryLabel.setText(formatTripSummary(trip));
        detailReviewLabel.setText(formatReview(trip.review().orElse(null)));
        statusLabel.getStyleClass().removeAll("status-current", "status-future", "status-past");
        statusLabel.getStyleClass().add("status-past");
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
     * Formats the selected Trip's inclusive date range.
     *
     * @param trip Trip whose dates are formatted.
     * @return Human-readable date range.
     */
    private static String formatDateRange(Trip trip) {
        return DATE_FORMATTER.format(trip.startDate()) + " – " + DATE_FORMATTER.format(trip.endDate());
    }

    /**
     * Formats a short summary for the selected Trip.
     *
     * @param trip Trip whose summary is formatted.
     * @return Human-readable Trip summary.
     */
    private static String formatTripSummary(Trip trip) {
        String planCount = trip.plans().size() == 1 ? "1 plan" : trip.plans().size() + " plans";
        return planCount + " · A trip worth remembering.";
    }

    /**
     * Formats an optional Trip review for the detail pane.
     *
     * @param review Review to display, or null when no review exists.
     * @return Human-readable review text.
     */
    private static String formatReview(Review review) {
        if (review == null) {
            return "No review recorded yet.";
        }
        String rating = review.rating().isPresent()
                ? "Rating: " + review.rating().getAsInt() + "/5"
                : "No rating";
        String text = review.text().orElse("No written review");
        return rating + "\n" + text;
    }

    /**
     * Shows an actionable error when Gallery data cannot be loaded.
     */
    private static void showLoadError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Gallery unavailable");
        alert.setHeaderText("Your past trips could not be loaded.");
        alert.setContentText("Check that the database is accessible, then try again.");
        alert.showAndWait();
    }
}
