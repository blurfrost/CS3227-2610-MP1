package doggo.ui.javafx;

import java.util.Objects;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Maintains the adaptive sizing of a Trip review card.
 */
final class ReviewCardSupport {
    /**
     * Maximum height of a Trip review card, including its heading and padding.
     */
    static final double MAX_CARD_HEIGHT = 200.0;
    private static final double DEFAULT_SCROLL_PANE_CHROME_HEIGHT = 2.0;

    private ReviewCardSupport() {
    }

    /**
     * Configures a review card to fit its wrapped content up to its maximum height.
     *
     * @param reviewCard Card containing the review heading and body.
     * @param reviewScrollPane Scrollable review body container.
     * @param reviewLabel Label displaying the review text.
     */
    static void configure(VBox reviewCard, ScrollPane reviewScrollPane, Label reviewLabel) {
        Objects.requireNonNull(reviewCard);
        Objects.requireNonNull(reviewScrollPane);
        Objects.requireNonNull(reviewLabel);

        reviewCard.setMaxHeight(MAX_CARD_HEIGHT);
        reviewLabel.setMinHeight(Region.USE_PREF_SIZE);

        InvalidationListener resizeListener = observable -> updateSize(reviewCard, reviewScrollPane,
                reviewLabel);
        reviewLabel.textProperty().addListener(resizeListener);
        reviewCard.widthProperty().addListener(resizeListener);
        reviewScrollPane.widthProperty().addListener(resizeListener);
        reviewScrollPane.viewportBoundsProperty().addListener(resizeListener);
        Platform.runLater(() -> updateSize(reviewCard, reviewScrollPane, reviewLabel));
    }

    /**
     * Recomputes the card and scrollable body heights from the current review content.
     *
     * @param reviewCard Card containing the review heading and body.
     * @param reviewScrollPane Scrollable review body container.
     * @param reviewLabel Label displaying the review text.
     */
    private static void updateSize(VBox reviewCard, ScrollPane reviewScrollPane, Label reviewLabel) {
        double textWidth = getTextWidth(reviewScrollPane, reviewLabel);
        if (textWidth <= 0) {
            return;
        }

        Node heading = reviewCard.getChildren().isEmpty() ? null : reviewCard.getChildren().getFirst();
        double headingHeight = heading == null ? 0 : heading.prefHeight(-1);
        double cardInsets = reviewCard.getInsets().getTop() + reviewCard.getInsets().getBottom();
        double spacing = reviewCard.getChildren().size() <= 1
                ? 0
                : reviewCard.getSpacing() * (reviewCard.getChildren().size() - 1);
        double cardChromeHeight = cardInsets + headingHeight + spacing;
        double contentHeight = reviewLabel.prefHeight(textWidth);
        double scrollPaneChromeHeight = getScrollPaneChromeHeight(reviewScrollPane);
        double maxBodyHeight = Math.max(0, MAX_CARD_HEIGHT - cardChromeHeight);
        double desiredBodyHeight = Math.min(maxBodyHeight, contentHeight + scrollPaneChromeHeight);
        double desiredCardHeight = cardChromeHeight + desiredBodyHeight;

        setHeight(reviewCard, desiredCardHeight);
        setHeight(reviewScrollPane, desiredBodyHeight);
    }

    /**
     * Returns the width available for wrapping the review text.
     *
     * @param reviewScrollPane Scrollable review body container.
     * @param reviewLabel Label displaying the review text.
     * @return Width available for the review label, or zero before layout.
     */
    private static double getTextWidth(ScrollPane reviewScrollPane, Label reviewLabel) {
        double viewportWidth = reviewScrollPane.getViewportBounds().getWidth();
        if (viewportWidth > 0) {
            return viewportWidth;
        }

        double scrollPaneWidth = reviewScrollPane.getWidth()
                - reviewScrollPane.getInsets().getLeft()
                - reviewScrollPane.getInsets().getRight();
        return scrollPaneWidth > 0 ? scrollPaneWidth : reviewLabel.getWidth();
    }

    /**
     * Returns the non-content height reserved by the ScrollPane skin.
     *
     * @param reviewScrollPane Scrollable review body container.
     * @return ScrollPane height not available to its viewport.
     */
    private static double getScrollPaneChromeHeight(ScrollPane reviewScrollPane) {
        double chromeHeight = reviewScrollPane.getHeight()
                - reviewScrollPane.getViewportBounds().getHeight();
        return chromeHeight > 0 ? chromeHeight : DEFAULT_SCROLL_PANE_CHROME_HEIGHT;
    }

    /**
     * Updates a Region's minimum and preferred heights only when the requested value changes.
     *
     * @param region Region whose preferred height is updated.
     * @param height Requested preferred height.
     */
    private static void setHeight(Region region, double height) {
        if (Math.abs(region.getPrefHeight() - height) > 0.01) {
            region.setPrefHeight(height);
        }
        if (Math.abs(region.getMinHeight() - height) > 0.01) {
            region.setMinHeight(height);
        }
    }
}
