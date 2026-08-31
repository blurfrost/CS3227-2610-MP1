package doggo.ui.javafx;

import java.util.Objects;

import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;

/**
 * Configures and updates labels used in compact GUI cards.
 */
final class CompactLabelSupport {
    private CompactLabelSupport() {
    }

    /**
     * Configures a label to remain on one line and render overflow with an ellipsis.
     *
     * @param label Label to configure.
     */
    static void configure(Label label) {
        Objects.requireNonNull(label);
        label.setWrapText(false);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Sets a label's text and exposes the complete value through a tooltip.
     *
     * @param label Label to update.
     * @param text Complete text to display.
     */
    static void setText(Label label, String text) {
        Objects.requireNonNull(label);
        Objects.requireNonNull(text);
        label.setText(text);
        label.setTooltip(new Tooltip(text));
    }
}
