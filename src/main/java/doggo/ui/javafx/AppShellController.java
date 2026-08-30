package doggo.ui.javafx;

import java.util.List;
import java.util.Objects;

import doggo.application.DoggoService;
import doggo.domain.Trip;
import doggo.domain.TripStatus;
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
     * Sidebar button for creating a Trip.
     */
    @FXML
    private Button newTripButton;

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
     * Organise controller included in the shell.
     */
    @FXML
    private OrganiseController organisePageController;

    /**
     * Gallery controller included in the shell.
     */
    @FXML
    private GalleryController galleryPageController;

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
        organisePageController.refresh();
    }

    /**
     * Shows the Gallery page and refreshes its data when selected.
     */
    @FXML
    private void handleGallery() {
        showPage(galleryPage, galleryButton);
        galleryPageController.refresh();
    }

    /**
     * Opens the modal form for creating a Trip from any application mode.
     */
    @FXML
    private void handleNewTrip() {
        TripCreationDialog dialog = new TripCreationDialog(service,
                newTripButton.getScene().getWindow());
        dialog.showAndWait().ifPresent(this::handleCreatedTrip);
    }

    /**
     * Refreshes the appropriate view after a Trip is created.
     *
     * @param trip Created Trip.
     */
    private void handleCreatedTrip(Trip trip) {
        if (service.getTripStatus(trip) == TripStatus.PAST) {
            showGalleryForTrip(trip);
            return;
        }
        showPage(organisePage, organiseButton);
        organisePageController.refreshAndSelect(trip.id());
    }

    /**
     * Shows Gallery after a newly created Trip is classified as past.
     *
     * @param trip Created past Trip.
     */
    private void showGalleryForTrip(Trip trip) {
        showPage(galleryPage, galleryButton);
        galleryPageController.refreshAndSelect(trip.id());
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
