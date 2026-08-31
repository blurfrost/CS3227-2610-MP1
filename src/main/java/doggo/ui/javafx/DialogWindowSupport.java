package doggo.ui.javafx;

import java.util.Objects;

import javafx.scene.Node;
import javafx.scene.control.Dialog;
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
        Objects.requireNonNull(dialog);
        Objects.requireNonNull(focusTarget);
        dialog.setResizable(true);
        dialog.setOnShown(event -> {
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            if (dialogStage.isShowing()) {
                setOpeningSizeAsMinimum(dialogStage);
            } else {
                dialogStage.setOnShown(windowEvent -> setOpeningSizeAsMinimum(dialogStage));
            }
            focusTarget.requestFocus();
        });
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
