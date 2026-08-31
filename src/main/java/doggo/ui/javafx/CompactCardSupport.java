package doggo.ui.javafx;

import java.util.Objects;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Orientation;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;

/**
 * Constrains compact cards to the usable width of their list cells.
 */
final class CompactCardSupport {
    private static final double WIDTH_SAFETY_MARGIN = 4;

    private CompactCardSupport() {
    }

    /**
     * Binds a card's preferred width to its list cell's usable width.
     *
     * @param card Card to constrain.
     * @param cell List cell that owns the card.
     */
    static void bindWidthToCell(Region card, ListCell<?> cell) {
        Objects.requireNonNull(card);
        Objects.requireNonNull(cell);
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        cell.setMinWidth(0);
        cell.setPrefWidth(0);
        cell.setMaxWidth(Double.MAX_VALUE);
        ListView<?> listView = cell.getListView();
        if (listView == null) {
            card.prefWidthProperty().bind(createCellWidthBinding(cell));
            cell.listViewProperty().addListener((observable, previousList, currentList) -> {
                if (currentList != null) {
                    bindToListView(card, cell, currentList);
                }
            });
        } else {
            bindToListView(card, cell, listView);
        }
    }

    /**
     * Binds a card and its cell to the width of their containing ListView.
     *
     * @param card Card whose width is being constrained.
     * @param cell List cell whose width is being constrained.
     * @param listView ListView containing the cell.
     */
    private static void bindToListView(Region card, ListCell<?> cell, ListView<?> listView) {
        ScrollBar verticalScrollBar = findVerticalScrollBar(listView);
        if (verticalScrollBar == null) {
            bindToCell(card, cell);
            return;
        }
        DoubleBinding widthBinding = createListWidthBinding(listView, verticalScrollBar);
        card.prefWidthProperty().unbind();
        card.prefWidthProperty().bind(widthBinding);
        card.maxWidthProperty().unbind();
        card.maxWidthProperty().bind(widthBinding);
        cell.prefWidthProperty().unbind();
        cell.prefWidthProperty().bind(widthBinding);
        cell.maxWidthProperty().unbind();
        cell.maxWidthProperty().bind(widthBinding);
    }

    /**
     * Binds a card to the usable width of its cell.
     *
     * @param card Card whose width is being constrained.
     * @param cell List cell containing the card.
     */
    private static void bindToCell(Region card, ListCell<?> cell) {
        DoubleBinding widthBinding = createCellWidthBinding(cell);
        card.prefWidthProperty().unbind();
        card.prefWidthProperty().bind(widthBinding);
        card.maxWidthProperty().unbind();
        card.maxWidthProperty().bind(widthBinding);
    }

    /**
     * Returns the vertical scrollbar installed by the specified ListView skin.
     *
     * @param listView ListView whose scrollbar is required.
     * @return Vertical scrollbar, or null when the skin has not installed one.
     */
    private static ScrollBar findVerticalScrollBar(ListView<?> listView) {
        return listView.lookupAll(".scroll-bar").stream()
                .filter(ScrollBar.class::isInstance)
                .map(ScrollBar.class::cast)
                .filter(scrollBar -> scrollBar.getOrientation() == Orientation.VERTICAL)
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates a fallback width binding used before a cell is attached to a ListView.
     *
     * @param cell List cell that owns the card.
     * @return Width binding for the unattached cell.
     */
    private static DoubleBinding createCellWidthBinding(ListCell<?> cell) {
        return Bindings.createDoubleBinding(
                () -> Math.max(0, cell.getWidth() - cell.getInsets().getLeft() - cell.getInsets().getRight()
                        - WIDTH_SAFETY_MARGIN),
                cell.widthProperty(), cell.insetsProperty());
    }

    /**
     * Creates a width binding for the ListView containing a cell.
     *
     * @param listView ListView whose width bounds the card.
     * @param verticalScrollBar Vertical scrollbar that reduces the usable width.
     * @return Width binding for the ListView.
     */
    private static DoubleBinding createListWidthBinding(
            ListView<?> listView, ScrollBar verticalScrollBar) {
        return Bindings.createDoubleBinding(
                () -> Math.max(0, listView.getWidth() - listView.getInsets().getLeft()
                        - listView.getInsets().getRight()
                        - (verticalScrollBar.isVisible() ? verticalScrollBar.getWidth() : 0)
                        - WIDTH_SAFETY_MARGIN),
                listView.widthProperty(), listView.insetsProperty(),
                verticalScrollBar.visibleProperty(), verticalScrollBar.widthProperty());
    }
}
