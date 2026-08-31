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
    private final Button editButton = new Button("Edit");
    private final HBox dateAndTime = new HBox(dateLabel, timeLabel);
    private final VBox details = new VBox(dateAndTime, destinationLabel);
    private final HBox card = new HBox(details, editButton);
    private final Consumer<Plan> editHandler;

    /**
     * Creates a Plan cell with the shared itinerary card style classes.
     */
    public PlanCell() {
        this(plan -> { });
    }

    /**
     * Creates a Plan cell that invokes the specified edit handler.
     *
     * @param editHandler Action invoked with the Plan when Edit is pressed.
     */
    public PlanCell(Consumer<Plan> editHandler) {
        this.editHandler = Objects.requireNonNull(editHandler);
        dateLabel.getStyleClass().add("plan-date");
        destinationLabel.getStyleClass().add("plan-destination");
        CompactLabelSupport.configure(destinationLabel);
        timeLabel.getStyleClass().add("plan-time");
        editButton.getStyleClass().addAll("secondary-button", "plan-edit-button");
        editButton.setOnAction(event -> {
            Plan plan = getItem();
            if (plan != null) {
                this.editHandler.accept(plan);
            }
        });
        HBox.setHgrow(details, Priority.ALWAYS);
        details.setMinWidth(0);
        card.getStyleClass().add("plan-card");
        card.setAlignment(Pos.CENTER_LEFT);
        details.setSpacing(4);
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
        setGraphic(card);
    }
}
