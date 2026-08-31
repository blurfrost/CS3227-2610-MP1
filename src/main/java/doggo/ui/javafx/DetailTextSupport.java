package doggo.ui.javafx;

import java.util.Objects;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Provides character-level wrapping for text displayed in detail panes.
 */
final class DetailTextSupport {
    private DetailTextSupport() {
    }

    /**
     * Sets a TextFlow's complete text using individually wrappable characters.
     *
     * @param textFlow TextFlow to update.
     * @param text Complete text to display.
     * @param textStyleClass Style class applied to each character.
     */
    static void setText(TextFlow textFlow, String text, String textStyleClass) {
        Objects.requireNonNull(textFlow);
        Objects.requireNonNull(text);
        Objects.requireNonNull(textStyleClass);
        textFlow.getChildren().setAll(createTextNodes(text, textStyleClass));
    }

    /**
     * Sets a TextFlow's label and value while keeping the value individually wrappable.
     *
     * @param textFlow TextFlow to update.
     * @param label Label text to display before the value.
     * @param value Value text to display after the label.
     * @param labelStyleClass Style class applied to the label.
     * @param valueStyleClass Style class applied to each value character.
     */
    static void setLabeledText(TextFlow textFlow, String label, String value,
                               String labelStyleClass, String valueStyleClass) {
        Objects.requireNonNull(textFlow);
        Objects.requireNonNull(label);
        Objects.requireNonNull(value);
        Objects.requireNonNull(labelStyleClass);
        Objects.requireNonNull(valueStyleClass);

        Text[] valueNodes = createTextNodes(value, valueStyleClass);
        Text[] textNodes = new Text[valueNodes.length + 1];
        textNodes[0] = createTextNode(label, labelStyleClass);
        System.arraycopy(valueNodes, 0, textNodes, 1, valueNodes.length);
        textFlow.getChildren().setAll(textNodes);
    }

    /**
     * Creates styled Text nodes for each Unicode code point in the text.
     *
     * @param text Text to split into wrappable nodes.
     * @param textStyleClass Style class applied to each node.
     * @return Text nodes representing the text.
     */
    private static Text[] createTextNodes(String text, String textStyleClass) {
        Text[] textNodes = text.codePoints()
                .mapToObj(codePoint -> createTextNode(codePoint, textStyleClass))
                .toArray(Text[]::new);
        return textNodes;
    }

    /**
     * Creates one styled Text node for a Unicode code point.
     *
     * @param codePoint Unicode code point to render.
     * @param textStyleClass Style class applied to the node.
     * @return Styled Text node.
     */
    private static Text createTextNode(int codePoint, String textStyleClass) {
        Text textNode = new Text(Character.toString(codePoint));
        textNode.getStyleClass().add(textStyleClass);
        return textNode;
    }

    /**
     * Creates one styled Text node for a complete label.
     *
     * @param text Label text to render.
     * @param textStyleClass Style class applied to the label.
     * @return Styled label Text node.
     */
    private static Text createTextNode(String text, String textStyleClass) {
        Text textNode = new Text(text);
        textNode.getStyleClass().add(textStyleClass);
        return textNode;
    }
}
