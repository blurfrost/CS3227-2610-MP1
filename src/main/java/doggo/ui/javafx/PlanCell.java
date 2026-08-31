package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

import doggo.domain.Plan;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Renders one Plan as a compact itinerary card.
 */
public final class PlanCell extends ListCell<Plan> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu (EEE)");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Label dateLabel = new Label();
    private final Label destinationLabel = new Label();
    private final Label timeLabel = new Label();
    private final Label reviewCueLabel = new Label();
    private final Button detailsButton = new Button("Details");
    private final HBox dateAndTime = new HBox(dateLabel, timeLabel, reviewCueLabel);
    private final VBox details = new VBox(dateAndTime, destinationLabel);
    private final HBox actions = new HBox(detailsButton);
    private final HBox card = new HBox(details, actions);
    private final Consumer<Plan> detailsHandler;

    /**
     * Creates a Plan cell with the shared itinerary card style classes.
     */
    public PlanCell() {
        this(plan -> { });
    }

    /**
     * Creates a Plan cell that invokes the specified details handler.
     *
     * @param detailsHandler Action invoked with the Plan when Details is pressed.
     */
    public PlanCell(Consumer<Plan> detailsHandler) {
        this.detailsHandler = Objects.requireNonNull(detailsHandler);
        configureCell();
    }

    /**
     * Configures the compact Plan cell controls and layout.
     */
    private void configureCell() {
        dateLabel.getStyleClass().add("plan-date");
        destinationLabel.getStyleClass().add("plan-destination");
        CompactLabelSupport.configure(destinationLabel);
        timeLabel.getStyleClass().add("plan-time");
        reviewCueLabel.getStyleClass().add("plan-review-cue");
        reviewCueLabel.setMinWidth(Region.USE_PREF_SIZE);
        reviewCueLabel.setMaxWidth(Region.USE_PREF_SIZE);
        detailsButton.getStyleClass().addAll("secondary-button", "plan-details-button");
        detailsButton.setAccessibleText("View Plan details");
        detailsButton.setMinWidth(Region.USE_PREF_SIZE);
        detailsButton.setOnAction(event -> {
            Plan plan = getItem();
            if (plan != null) {
                this.detailsHandler.accept(plan);
            }
        });
        HBox.setHgrow(details, Priority.ALWAYS);
        details.setMinWidth(0);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setSpacing(8);
        actions.setMinWidth(Region.USE_PREF_SIZE);
        card.getStyleClass().add("plan-card");
        card.setAlignment(Pos.CENTER_LEFT);
        details.setSpacing(4);
        dateAndTime.setAlignment(Pos.BASELINE_LEFT);
        dateAndTime.setSpacing(8);
        CompactCardSupport.bindWidthToCell(card, this);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /**
     * Updates this cell with the specified Plan.
     *
     * @param plan Plan to render.
     * @param isEmpty Whether the cell has no Plan.
     */
    @Override
    protected void updateItem(Plan plan, boolean isEmpty) {
        super.updateItem(plan, isEmpty);
        if (isEmpty || plan == null) {
            setGraphic(null);
            return;
        }
        dateLabel.setText(DATE_FORMATTER.format(plan.date()));
        CompactLabelSupport.setText(destinationLabel, plan.destination());
        timeLabel.setText(TIME_FORMATTER.format(plan.time()));
        reviewCueLabel.setText(ReviewDisplaySupport.formatCue(plan.review()));
        reviewCueLabel.setVisible(plan.review().isPresent());
        reviewCueLabel.setManaged(plan.review().isPresent());
        setGraphic(card);
    }
}
