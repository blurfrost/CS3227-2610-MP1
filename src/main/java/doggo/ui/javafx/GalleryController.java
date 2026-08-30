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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

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
        tripList.setCellFactory(list -> new TripCell(service));
        planList.setCellFactory(list -> new PlanCell());
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
        try {
            List<Trip> trips = service.getPastTrips();
            tripList.getItems().setAll(trips);
            boolean isEmpty = trips.isEmpty();
            emptyState.setVisible(isEmpty);
            emptyState.setManaged(isEmpty);
            if (isEmpty) {
                showDetails(null);
            } else {
                selectTrip(trips, selectedTripId);
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
     */
    private void selectTrip(List<Trip> trips, UUID selectedTripId) {
        int selectedIndex = selectedTripId == null
                ? 0
                : findTripIndex(trips, selectedTripId);
        tripList.getSelectionModel().select(selectedIndex < 0 ? 0 : selectedIndex);
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

        List<Plan> plans = service.getPlans(trip);
        detailTitleLabel.setText(trip.title());
        detailDatesLabel.setText(formatDateRange(trip));
        detailSummaryLabel.setText(formatTripSummary(trip));
        detailReviewLabel.setText(formatReview(trip.review().orElse(null)));
        statusLabel.getStyleClass().removeAll("status-current", "status-future", "status-past");
        statusLabel.getStyleClass().add("status-past");
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
