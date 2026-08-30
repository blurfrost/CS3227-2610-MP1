package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;

import doggo.domain.Plan;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Renders one Plan as a compact itinerary card.
 */
public final class PlanCell extends ListCell<Plan> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Label dateLabel = new Label();
    private final Label destinationLabel = new Label();
    private final Label timeLabel = new Label();
    private final VBox card = new VBox(new HBox(dateLabel, timeLabel), destinationLabel);

    /**
     * Creates a Plan cell with the shared itinerary card style classes.
     */
    public PlanCell() {
        dateLabel.getStyleClass().add("plan-date");
        destinationLabel.getStyleClass().add("plan-destination");
        timeLabel.getStyleClass().add("plan-time");
        card.getStyleClass().add("plan-card");
        card.setSpacing(4);
        HBox dateAndTime = (HBox) card.getChildren().getFirst();
        dateAndTime.setSpacing(8);
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
        destinationLabel.setText(plan.destination());
        timeLabel.setText(TIME_FORMATTER.format(plan.time()));
        setGraphic(card);
    }
}
