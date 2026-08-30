package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Trip;
import doggo.domain.TripStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

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
    private Label detailTitleLabel;

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
     * Creates an Organise controller backed by the specified application service.
     *
     * @param service Application service used to query Trips and Plans.
     */
    public OrganiseController(DoggoService service) {
        this.service = Objects.requireNonNull(service);
    }

    /**
     * Loads current and future Trips and initializes selection handling.
     */
    @FXML
    private void initialize() {
        tripList.setCellFactory(list -> new TripCell(service));
        planList.setCellFactory(list -> new PlanCell());
        tripList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previousTrip, selectedTrip) -> showDetails(selectedTrip));
        loadTrips();
    }

    /**
     * Loads and displays the current Organise snapshot.
     */
    private void loadTrips() {
        try {
            List<Trip> trips = service.getCurrentAndFutureTrips();
            tripList.getItems().setAll(trips);
            boolean isEmpty = trips.isEmpty();
            emptyState.setVisible(isEmpty);
            emptyState.setManaged(isEmpty);
            if (isEmpty) {
                showDetails(null);
            } else {
                tripList.getSelectionModel().selectFirst();
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
     * Updates the detail pane for the selected Trip.
     *
     * @param trip Selected Trip, or null when nothing is selected.
     */
    private void showDetails(Trip trip) {
        boolean hasSelection = trip != null;
        detailEmptyState.setVisible(!hasSelection);
        detailEmptyState.setManaged(!hasSelection);
        detailContent.setVisible(hasSelection);
        detailContent.setManaged(hasSelection);
        statusLabel.setVisible(hasSelection);
        statusLabel.setManaged(hasSelection);
        if (!hasSelection) {
            return;
        }

        TripStatus status = service.getTripStatus(trip);
        List<Plan> plans = service.getPlans(trip);
        detailTitleLabel.setText(trip.title());
        detailDatesLabel.setText(formatDateRange(trip));
        detailSummaryLabel.setText(formatTripSummary(trip, status));
        statusLabel.setText(formatStatus(status));
        statusLabel.getStyleClass().removeAll("status-current", "status-future");
        statusLabel.getStyleClass().add(status == TripStatus.CURRENT
                ? "status-current"
                : "status-future");
        planList.getItems().setAll(plans);
        boolean isPlanEmpty = plans.isEmpty();
        planEmptyState.setVisible(isPlanEmpty);
        planEmptyState.setManaged(isPlanEmpty);
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
     * @param status Current status of the Trip.
     * @return Human-readable Trip summary.
     */
    private static String formatTripSummary(Trip trip, TripStatus status) {
        String planCount = trip.plans().size() == 1 ? "1 plan" : trip.plans().size() + " plans";
        String statusDescription = status == TripStatus.CURRENT
                ? "This trip is happening now."
                : "This trip is ready to be planned.";
        return planCount + " · " + statusDescription;
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
}
