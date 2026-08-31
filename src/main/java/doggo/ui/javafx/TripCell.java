package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;

import doggo.domain.Trip;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Renders one Trip as a selectable Organise card.
 */
public final class TripCell extends ListCell<Trip> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final Label titleLabel = new Label();
    private final Label datesLabel = new Label();
    private final HBox header = new HBox(titleLabel);
    private final VBox card = new VBox(header, datesLabel);

    /**
     * Creates a Trip cell for compact Trip list cards.
     */
    public TripCell() {
        titleLabel.getStyleClass().add("trip-title");
        CompactLabelSupport.configure(titleLabel);
        datesLabel.getStyleClass().add("trip-dates");
        header.getStyleClass().add("trip-card-header");
        header.setSpacing(8);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        card.getStyleClass().add("trip-card");
        card.setSpacing(6);
        CompactCardSupport.bindWidthToCell(card, this);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /**
     * Updates this cell with the specified Trip.
     *
     * @param trip Trip to render.
     * @param isEmpty Whether the cell has no Trip.
     */
    @Override
    protected void updateItem(Trip trip, boolean isEmpty) {
        super.updateItem(trip, isEmpty);
        if (isEmpty || trip == null) {
            setGraphic(null);
            return;
        }
        CompactLabelSupport.setText(titleLabel, trip.title());
        datesLabel.setText(DATE_FORMATTER.format(trip.startDate()) + " – "
                + DATE_FORMATTER.format(trip.endDate()));
        setGraphic(card);
    }
}
