package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import doggo.application.DashboardEntry;
import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Review;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

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
    private Label detailDestinationLabel;

    /**
     * Selected Plan Trip label.
     */
    @FXML
    private Label detailTripLabel;

    /**
     * Selected Plan schedule label.
     */
    @FXML
    private Label detailScheduleLabel;

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
        loadEntries();
    }

    /**
     * Loads and displays the current Dashboard snapshot.
     */
    private void loadEntries() {
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
                planList.getSelectionModel().selectFirst();
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
        if (!hasSelection) {
            return;
        }

        Plan plan = entry.plan();
        detailDestinationLabel.setText(plan.destination());
        detailTripLabel.setText("Trip · " + entry.tripTitle());
        detailScheduleLabel.setText(plan.date().format(DETAIL_DATE_FORMATTER)
                + " at " + plan.time().format(TIME_FORMATTER));
        detailReviewLabel.setText(formatReview(plan.review().orElse(null)));
    }

    /**
     * Formats an optional Plan review for the detail pane.
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
     * Shows an actionable error when Dashboard data cannot be loaded.
     */
    private static void showLoadError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Dashboard unavailable");
        alert.setHeaderText("Today's plans could not be loaded.");
        alert.setContentText("Check that the database is accessible, then try again.");
        alert.showAndWait();
    }
}
