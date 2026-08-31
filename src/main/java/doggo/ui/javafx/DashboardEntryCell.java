package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;

import doggo.application.DashboardEntry;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

/**
 * Renders one Dashboard entry as a compact Plan card.
 */
public final class DashboardEntryCell extends ListCell<DashboardEntry> {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Label timeLabel = new Label();
    private final Label destinationLabel = new Label();
    private final Label tripLabel = new Label();
    private final VBox card = new VBox(timeLabel, destinationLabel, tripLabel);

    /**
     * Creates a Dashboard entry cell with the shared card style classes.
     */
    public DashboardEntryCell() {
        timeLabel.getStyleClass().add("plan-time");
        destinationLabel.getStyleClass().add("plan-destination");
        CompactLabelSupport.configure(destinationLabel);
        tripLabel.getStyleClass().add("plan-trip");
        CompactLabelSupport.configure(tripLabel);
        card.getStyleClass().add("plan-card");
        card.setSpacing(4);
        CompactCardSupport.bindWidthToCell(card, this);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /**
     * Updates this cell with the specified Dashboard entry.
     *
     * @param entry Dashboard entry to render.
     * @param isEmpty Whether the cell has no entry.
     */
    @Override
    protected void updateItem(DashboardEntry entry, boolean isEmpty) {
        super.updateItem(entry, isEmpty);
        if (isEmpty || entry == null) {
            setGraphic(null);
            return;
        }
        timeLabel.setText(entry.plan().time().format(TIME_FORMATTER));
        CompactLabelSupport.setText(destinationLabel, entry.plan().destination());
        CompactLabelSupport.setText(tripLabel, entry.tripTitle());
        setGraphic(card);
    }
}
