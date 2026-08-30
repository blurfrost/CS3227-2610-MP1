package doggo.ui.javafx;

import java.util.List;
import java.util.Objects;

import doggo.application.DoggoService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * Controls the persistent navigation and page visibility of the doggo shell.
 */
public final class AppShellController {
    private static final String ACTIVE_STYLE_CLASS = "active";

    /**
     * Application service provided to the shell for its child views.
     */
    private final DoggoService service;

    /**
     * Navigation buttons displayed in the sidebar.
     */
    private List<Button> navigationButtons;

    /**
     * Dashboard navigation button.
     */
    @FXML
    private Button dashboardButton;

    /**
     * Organise navigation button.
     */
    @FXML
    private Button organiseButton;

    /**
     * Gallery navigation button.
     */
    @FXML
    private Button galleryButton;

    /**
     * Dashboard page.
     */
    @FXML
    private VBox dashboardPage;

    /**
     * Organise page.
     */
    @FXML
    private VBox organisePage;

    /**
     * Gallery page.
     */
    @FXML
    private VBox galleryPage;

    /**
     * Creates a shell controller backed by the specified application service.
     *
     * @param service Application service shared by the JavaFX views.
     */
    public AppShellController(DoggoService service) {
        this.service = Objects.requireNonNull(service);
    }

    /**
     * Initializes the navigation state after FXML injects the controls.
     */
    @FXML
    private void initialize() {
        navigationButtons = List.of(dashboardButton, organiseButton, galleryButton);
        showPage(dashboardPage, dashboardButton);
    }

    /**
     * Shows the Dashboard page when its navigation button is selected.
     */
    @FXML
    private void handleDashboard() {
        showPage(dashboardPage, dashboardButton);
    }

    /**
     * Shows the Organise page when its navigation button is selected.
     */
    @FXML
    private void handleOrganise() {
        showPage(organisePage, organiseButton);
    }

    /**
     * Shows the Gallery placeholder when its navigation button is selected.
     */
    @FXML
    private void handleGallery() {
        showPage(galleryPage, galleryButton);
    }

    /**
     * Makes one page visible and updates the active navigation style.
     *
     * @param page Page to display.
     * @param activeButton Navigation button associated with the page.
     */
    private void showPage(VBox page, Button activeButton) {
        setPageVisible(dashboardPage, page == dashboardPage);
        setPageVisible(organisePage, page == organisePage);
        setPageVisible(galleryPage, page == galleryPage);
        navigationButtons.forEach(button -> button.getStyleClass().remove(ACTIVE_STYLE_CLASS));
        activeButton.getStyleClass().add(ACTIVE_STYLE_CLASS);
    }

    /**
     * Sets whether a page participates in layout and rendering.
     *
     * @param page Page to update.
     * @param isVisible Whether the page should be visible.
     */
    private static void setPageVisible(Node page, boolean isVisible) {
        page.setVisible(isVisible);
        page.setManaged(isVisible);
    }
}
