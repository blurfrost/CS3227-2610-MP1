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
    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("Delete");
    private final HBox dateAndTime = new HBox(dateLabel, timeLabel);
    private final VBox details = new VBox(dateAndTime, destinationLabel);
    private final HBox actions = new HBox(editButton, deleteButton);
    private final HBox card = new HBox(details, actions);
    private final Consumer<Plan> editHandler;
    private final Consumer<Plan> deleteHandler;

    /**
     * Creates a Plan cell with the shared itinerary card style classes.
     */
    public PlanCell() {
        this(plan -> { }, plan -> { });
    }

    /**
     * Creates a Plan cell that invokes the specified edit handler.
     *
     * @param editHandler Action invoked with the Plan when Edit is pressed.
     */
    public PlanCell(Consumer<Plan> editHandler) {
        this(editHandler, plan -> { });
    }

    /**
     * Creates a Plan cell that invokes the specified Edit and Delete handlers.
     *
     * @param editHandler Action invoked with the Plan when Edit is pressed.
     * @param deleteHandler Action invoked with the Plan when Delete is pressed.
     */
    public PlanCell(Consumer<Plan> editHandler, Consumer<Plan> deleteHandler) {
        this.editHandler = Objects.requireNonNull(editHandler);
        this.deleteHandler = Objects.requireNonNull(deleteHandler);
        dateLabel.getStyleClass().add("plan-date");
        destinationLabel.getStyleClass().add("plan-destination");
        CompactLabelSupport.configure(destinationLabel);
        timeLabel.getStyleClass().add("plan-time");
        editButton.getStyleClass().addAll("secondary-button", "plan-edit-button");
        deleteButton.getStyleClass().addAll("danger-button", "plan-delete-button");
        editButton.setMinWidth(Region.USE_PREF_SIZE);
        deleteButton.setMinWidth(Region.USE_PREF_SIZE);
        editButton.setOnAction(event -> {
            Plan plan = getItem();
            if (plan != null) {
                this.editHandler.accept(plan);
            }
        });
        deleteButton.setOnAction(event -> {
            Plan plan = getItem();
            if (plan != null) {
                this.deleteHandler.accept(plan);
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
