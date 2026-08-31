package doggo.ui.javafx;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;

import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.storage.SqliteTripRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Starts the doggo JavaFX application.
 */
public final class DoggoApplication extends Application {
    private static final Path DEFAULT_DATABASE_PATH = Path.of("data", "doggo.db");
    private static final double INITIAL_WIDTH = 1180;
    private static final double INITIAL_HEIGHT = 760;

    /**
     * Application service shared by the JavaFX views.
     */
    private DoggoService service;

    /**
     * Creates the JavaFX application with the default constructor required by JavaFX.
     */
    public DoggoApplication() {
    }

    /**
     * Starts the primary doggo window and initializes its application service.
     *
     * @param stage Primary JavaFX stage.
     */
    @Override
    public void start(Stage stage) {
        try {
            service = new DoggoService(
                    new SqliteTripRepository(DEFAULT_DATABASE_PATH),
                    Clock.systemDefaultZone());
        } catch (RepositoryException exception) {
            showStartupError("The database could not be opened.",
                    "Check that the database location is accessible, then try again.");
            return;
        }

        try {
            stage.setTitle("doggo");
            stage.setScene(createInitialScene());
            stage.show();
            setOpeningSizeAsMinimum(stage);
        } catch (IOException exception) {
            showStartupError("The application interface could not be loaded.",
                    "Restart doggo and try again.");
        }
    }

    /**
     * Sets the current dimensions of the primary window as its minimum dimensions.
     *
     * @param stage Primary window to constrain.
     */
    static void setOpeningSizeAsMinimum(Stage stage) {
        Objects.requireNonNull(stage);
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
    }

    /**
     * Loads the FXML root scene and supplies the initialized application service.
     *
     * @return Loaded doggo scene.
     * @throws IOException If the FXML resource cannot be loaded.
     */
    private Scene createInitialScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(DoggoApplication.class.getResource("AppShell.fxml"));
        loader.setControllerFactory(type -> {
            if (type == AppShellController.class) {
                return new AppShellController(service);
            }
            if (type == DashboardController.class) {
                return new DashboardController(service);
            }
            if (type == OrganiseController.class) {
                return new OrganiseController(service);
            }
            if (type == GalleryController.class) {
                return new GalleryController(service);
            }
            throw new IllegalArgumentException("Unsupported FXML controller: " + type.getName());
        });
        Parent root = loader.load();
        return new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);
    }

    /**
     * Displays a recoverable startup error and closes the JavaFX runtime.
     *
     * @param header Error summary.
     * @param content Recovery guidance.
     */
    private static void showStartupError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Unable to start doggo");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
        Platform.exit();
    }
}
