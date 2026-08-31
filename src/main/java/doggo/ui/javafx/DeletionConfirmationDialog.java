package doggo.ui.javafx;

import java.util.Objects;

import doggo.domain.Plan;
import doggo.domain.Trip;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Displays a confirmation dialog for deleting a Trip or Plan.
 */
final class DeletionConfirmationDialog extends Dialog<Boolean> {
    private static final ButtonType YES_BUTTON_TYPE = new ButtonType("Yes", ButtonBar.ButtonData.YES);
    private static final ButtonType NO_BUTTON_TYPE = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

    /**
     * Creates a deletion confirmation dialog with the specified message.
     *
     * @param title Dialog title.
     * @param description Confirmation message.
     * @param owner Window that owns this dialog.
     */
    private DeletionConfirmationDialog(String title, String description, Window owner) {
        initOwner(Objects.requireNonNull(owner));
        initModality(Modality.WINDOW_MODAL);
        setTitle(Objects.requireNonNull(title));

        DialogPane dialogPane = new DialogPane() {
            @Override
            protected Node createButtonBar() {
                ButtonBar buttonBar = (ButtonBar) super.createButtonBar();
                buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
                return buttonBar;
            }
        };
        setDialogPane(dialogPane);
        dialogPane.getButtonTypes().addAll(YES_BUTTON_TYPE, NO_BUTTON_TYPE);
        dialogPane.setContent(createDescription(description));
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(DeletionConfirmationDialog.class.getResource("doggo.css")).toExternalForm());

        Button yesButton = (Button) dialogPane.lookupButton(YES_BUTTON_TYPE);
        Button noButton = (Button) dialogPane.lookupButton(NO_BUTTON_TYPE);
        yesButton.getStyleClass().addAll("danger-button", "delete-confirm-button");
        noButton.getStyleClass().addAll("secondary-button", "delete-cancel-button");
        noButton.setDefaultButton(true);
        noButton.setCancelButton(true);
        setResultConverter(buttonType -> buttonType == YES_BUTTON_TYPE);
    }

    /**
     * Creates a confirmation dialog for deleting the specified Plan.
     *
     * @param plan Plan to be deleted.
     * @param tripTitle Title of the Trip containing the Plan.
     * @param owner Window that owns this dialog.
     * @return Plan deletion confirmation dialog.
     */
    static DeletionConfirmationDialog forPlan(Plan plan, String tripTitle, Window owner) {
        Objects.requireNonNull(plan);
        Objects.requireNonNull(tripTitle);
        String description = "Do you really want to delete the plan \"" + plan.destination()
                + "\" from the trip \"" + tripTitle + "\"?";
        return new DeletionConfirmationDialog("Delete a plan", description, owner);
    }

    /**
     * Creates a confirmation dialog for deleting the specified Trip.
     *
     * @param trip Trip to be deleted.
     * @param owner Window that owns this dialog.
     * @return Trip deletion confirmation dialog.
     */
    static DeletionConfirmationDialog forTrip(Trip trip, Window owner) {
        Objects.requireNonNull(trip);
        String description = "Do you really want to delete the trip \"" + trip.title() + "\"?";
        return new DeletionConfirmationDialog("Delete a trip", description, owner);
    }

    /**
     * Shows a deletion confirmation for the specified Plan.
     *
     * @param plan Plan to be deleted.
     * @param tripTitle Title of the Trip containing the Plan.
     * @param owner Window that owns this dialog.
     * @return True when the user selects Yes, otherwise false.
     */
    static boolean confirmPlan(Plan plan, String tripTitle, Window owner) {
        return forPlan(plan, tripTitle, owner).showAndWait().orElse(false);
    }

    /**
     * Shows a deletion confirmation for the specified Trip.
     *
     * @param trip Trip to be deleted.
     * @param owner Window that owns this dialog.
     * @return True when the user selects Yes, otherwise false.
     */
    static boolean confirmTrip(Trip trip, Window owner) {
        return forTrip(trip, owner).showAndWait().orElse(false);
    }

    /**
     * Creates the wrapped description displayed by the confirmation dialog.
     *
     * @param description Confirmation message.
     * @return Wrapped confirmation description.
     */
    private static Label createDescription(String description) {
        Label descriptionLabel = new Label(Objects.requireNonNull(description));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(420);
        descriptionLabel.getStyleClass().add("delete-confirmation-text");
        return descriptionLabel;
    }
}
