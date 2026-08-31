package doggo.ui.javafx;

import java.util.Objects;

import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Provides shared window configuration for modal dialogs.
 */
final class DialogWindowSupport {
    private DialogWindowSupport() {
    }

    /**
     * Enables expansion while preserving the dialog's opening dimensions as minimums.
     *
     * @param dialog Dialog to configure.
     * @param focusTarget Control to focus when the dialog opens.
     */
    static void enableExpandOnly(Dialog<?> dialog, Node focusTarget) {
        enableExpandOnly(dialog, focusTarget, () -> { });
    }

    /**
     * Enables expansion and runs an additional action when the dialog opens.
     *
     * @param dialog Dialog to configure.
     * @param focusTarget Control to focus when the dialog opens.
     * @param shownHandler Action to run after the dialog opens.
     */
    static void enableExpandOnly(Dialog<?> dialog, Node focusTarget, Runnable shownHandler) {
        Objects.requireNonNull(dialog);
        Objects.requireNonNull(focusTarget);
        Objects.requireNonNull(shownHandler);
        dialog.setResizable(true);
        dialog.setOnShown(event -> {
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            if (dialogStage.isShowing()) {
                setOpeningSizeAsMinimum(dialogStage);
            } else {
                dialogStage.setOnShown(windowEvent -> setOpeningSizeAsMinimum(dialogStage));
            }
            focusTarget.requestFocus();
            shownHandler.run();
        });
    }

    /**
     * Resizes a shown dialog to fit its current content while preserving its width.
     *
     * @param dialog Dialog whose content determines the new height.
     */
    static void resizeToContent(Dialog<?> dialog) {
        Objects.requireNonNull(dialog);
        DialogPane dialogPane = dialog.getDialogPane();
        if (dialogPane.getScene() == null) {
            return;
        }

        Stage dialogStage = (Stage) dialogPane.getScene().getWindow();
        if (!dialogStage.isShowing()) {
            return;
        }

        double currentWidth = dialogStage.getWidth();
        double currentDialogPaneWidth = dialogPane.getWidth();
        dialogStage.setMinHeight(0);
        if (currentDialogPaneWidth > 0) {
            dialogPane.setPrefWidth(currentDialogPaneWidth);
        }
        dialogPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialogStage.sizeToScene();
        if (currentWidth > 0) {
            dialogStage.setWidth(currentWidth);
        }
        dialogStage.setMinHeight(dialogStage.getHeight());
    }

    /**
     * Sets the current dimensions of a dialog window as its minimum dimensions.
     *
     * @param dialogStage Dialog window to constrain.
     */
    static void setOpeningSizeAsMinimum(Stage dialogStage) {
        Objects.requireNonNull(dialogStage);
        dialogStage.setMinWidth(dialogStage.getWidth());
        dialogStage.setMinHeight(dialogStage.getHeight());
    }
}
