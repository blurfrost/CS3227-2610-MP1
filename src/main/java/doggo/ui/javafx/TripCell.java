package doggo.ui.javafx;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import doggo.application.DoggoService;
import doggo.domain.Trip;
import doggo.domain.TripStatus;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Renders one Trip as a selectable Organise card.
 */
public final class TripCell extends ListCell<Trip> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final DoggoService service;
    private final Label titleLabel = new Label();
    private final Label datesLabel = new Label();
    private final Label statusLabel = new Label();
    private final HBox header = new HBox(titleLabel, new Region(), statusLabel);
    private final VBox card = new VBox(header, datesLabel);

    /**
     * Creates a Trip cell backed by the specified application service.
     *
     * @param service Application service used to classify Trips.
     */
    public TripCell(DoggoService service) {
        this.service = Objects.requireNonNull(service);
        titleLabel.getStyleClass().add("trip-title");
        datesLabel.getStyleClass().add("trip-dates");
        statusLabel.getStyleClass().add("trip-status");
        header.getStyleClass().add("trip-card-header");
        header.setSpacing(8);
        HBox.setHgrow(header.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        card.getStyleClass().add("trip-card");
        card.setSpacing(6);
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
        TripStatus status = service.getTripStatus(trip);
        titleLabel.setText(trip.title());
        datesLabel.setText(DATE_FORMATTER.format(trip.startDate()) + " – "
                + DATE_FORMATTER.format(trip.endDate()));
        statusLabel.setText(formatStatus(status));
        statusLabel.getStyleClass().removeAll("status-current", "status-future", "status-past");
        statusLabel.getStyleClass().add(statusStyleClass(status));
        setGraphic(card);
    }

    /**
     * Formats a Trip status for the card badge.
     *
     * @param status Trip status to format.
     * @return Badge text.
     */
    private static String formatStatus(TripStatus status) {
        return switch (status) {
        case CURRENT -> "NOW";
        case FUTURE -> "UPCOMING";
        case PAST -> "COMPLETED";
        };
    }

    /**
     * Returns the style class matching a Trip status.
     *
     * @param status Trip status to style.
     * @return Status style class.
     */
    private static String statusStyleClass(TripStatus status) {
        return switch (status) {
        case CURRENT -> "status-current";
        case FUTURE -> "status-future";
        case PAST -> "status-past";
        };
    }
}
