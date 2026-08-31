package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Trip;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Displays a selected Plan and its available management actions.
 */
final class PlanDetailsDialog extends Dialog<PlanDetailsDialog.Result> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final ButtonType CLOSE_BUTTON_TYPE = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

    private final DoggoService service;
    private final Trip trip;
    private final TextFlow destinationLabel = new TextFlow();
    private final TextFlow tripLabel = new TextFlow();
    private final Label scheduleLabel = new Label();
    private final Button reviewButton = new Button();
    private final VBox reviewCard = new VBox();
    private final ScrollPane reviewScrollPane = new ScrollPane();
    private final Label reviewLabel = new Label();

    private Plan currentPlan;

    /**
     * Creates a Plan-details dialog for the specified Plan.
     *
     * @param service Application service used to persist Plan changes.
     * @param trip Trip containing the Plan.
     * @param plan Plan to display.
     * @param owner Window that owns this dialog.
     */
    PlanDetailsDialog(DoggoService service, Trip trip, Plan plan, Window owner) {
        this.service = Objects.requireNonNull(service);
        this.trip = Objects.requireNonNull(trip);
        this.currentPlan = Objects.requireNonNull(plan);
        initOwner(Objects.requireNonNull(owner));
        initModality(Modality.WINDOW_MODAL);
        setTitle("Plan details");

        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().add(CLOSE_BUTTON_TYPE);
        dialogPane.setContent(createContent());
        dialogPane.setPrefWidth(560);
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(PlanDetailsDialog.class.getResource("doggo.css")).toExternalForm());
        setResultConverter(buttonType -> buttonType == CLOSE_BUTTON_TYPE
                ? new Result(currentPlan.id(), false)
                : null);
        DialogWindowSupport.enableExpandOnly(this, reviewButton, this::scheduleContentResize);
        refreshContent();
    }

    /**
     * Creates the Plan details content and its action controls.
     *
     * @return Plan details content.
     */
    private VBox createContent() {
        destinationLabel.setMaxWidth(Double.MAX_VALUE);
        tripLabel.setMaxWidth(Double.MAX_VALUE);
        scheduleLabel.setWrapText(true);
        scheduleLabel.getStyleClass().add("detail-schedule");

        Button editButton = new Button("Edit plan");
        editButton.setId("planEditButton");
        editButton.getStyleClass().add("secondary-button");
        editButton.setOnAction(event -> handleEditPlan());

        reviewButton.getStyleClass().add("review-action-button");
        reviewButton.setId("planReviewButton");
        reviewButton.setOnAction(event -> handleReviewPlan());

        Button deleteButton = new Button("Delete plan");
        deleteButton.setId("planDeleteButton");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setOnAction(event -> handleDeletePlan());

        HBox actions = new HBox(8, editButton, reviewButton, deleteButton);
        createReviewCard();

        VBox content = new VBox(12, destinationLabel, tripLabel, scheduleLabel, actions, reviewCard);
        content.getStyleClass().add("plan-details");
        content.setPadding(new Insets(8, 10, 4, 10));
        return content;
    }

    /**
     * Creates the scrollable review card used by the inspector.
     */
    private void createReviewCard() {
        Label heading = new Label("Review");
        heading.getStyleClass().add("review-heading");
        reviewLabel.getStyleClass().add("review-body");
        reviewLabel.setWrapText(true);
        reviewLabel.setMinHeight(Region.USE_PREF_SIZE);
        reviewScrollPane.setFitToWidth(true);
        reviewScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        reviewScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        reviewScrollPane.setContent(reviewLabel);
        reviewScrollPane.getStyleClass().add("review-scroll-pane");
        reviewCard.getStyleClass().add("review-card");
        reviewCard.setSpacing(5);
        reviewCard.getChildren().setAll(heading, reviewScrollPane);
        ReviewCardSupport.configure(reviewCard, reviewScrollPane, reviewLabel);
    }

    /**
     * Refreshes all displayed values from the current Plan state.
     */
    private void refreshContent() {
        DetailTextSupport.setText(destinationLabel, currentPlan.destination(), "detail-destination-text");
        DetailTextSupport.setLabeledText(tripLabel, "From ", trip.title(),
                "detail-trip-text", "detail-trip-name-text");
        scheduleLabel.setText(DATE_FORMATTER.format(currentPlan.date())
                + " at " + TIME_FORMATTER.format(currentPlan.time()));
        boolean hasReview = currentPlan.review().isPresent();
        reviewButton.setText(hasReview ? "Edit Review" : "Add Review");
        reviewCard.setVisible(hasReview);
        reviewCard.setManaged(hasReview);
        reviewLabel.setText(hasReview ? ReviewDisplaySupport.format(currentPlan.review()) : "");
        scheduleContentResize();
    }

    /**
     * Schedules the dialog to fit its currently visible content.
     */
    private void scheduleContentResize() {
        Platform.runLater(() -> DialogWindowSupport.resizeToContent(this));
    }

    /**
     * Opens the shared Plan form and updates the inspector when editing succeeds.
     */
    private void handleEditPlan() {
        PlanCreationDialog dialog = new PlanCreationDialog(service, trip, currentPlan, getWindow());
        dialog.showAndWait().ifPresent(updatedPlan -> {
            currentPlan = updatedPlan;
            refreshContent();
        });
    }

    /**
     * Opens the shared Review form and updates the inspector when saving succeeds.
     */
    private void handleReviewPlan() {
        ReviewDialog<Plan> dialog = ReviewDialog.forPlan(service, trip, currentPlan, getWindow());
        dialog.showAndWait().ifPresent(updatedPlan -> {
            currentPlan = updatedPlan;
            refreshContent();
        });
    }

    /**
     * Confirms and deletes the Plan, then closes the inspector.
     */
    private void handleDeletePlan() {
        if (!DeletionConfirmationDialog.confirmPlan(currentPlan, trip.title(), getWindow())) {
            return;
        }
        try {
            service.deletePlan(trip.id(), currentPlan.id());
            setResult(new Result(currentPlan.id(), true));
            close();
        } catch (RepositoryException | IllegalArgumentException exception) {
            showDeleteError(exception.getMessage());
        }
    }

    /**
     * Returns the window currently displaying this dialog.
     *
     * @return Current dialog window.
     */
    private Window getWindow() {
        return getDialogPane().getScene().getWindow();
    }

    /**
     * Shows an actionable error when the Plan cannot be deleted.
     *
     * @param message Error message to display.
     */
    private void showDeleteError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(getWindow());
        alert.setTitle("Plan unavailable");
        alert.setHeaderText("The selected Plan could not be deleted.");
        alert.setContentText(message == null ? "Check that the database is accessible, then try again." : message);
        alert.showAndWait();
    }

    /**
     * Returns the result produced when the inspector closes.
     *
     * @param planId Plan identity displayed by the inspector.
     * @param isDeleted Whether the Plan was deleted.
     */
    record Result(UUID planId, boolean isDeleted) {
    }
}
