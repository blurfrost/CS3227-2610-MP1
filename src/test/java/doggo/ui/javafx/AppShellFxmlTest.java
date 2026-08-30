package doggo.ui.javafx;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.storage.InMemoryTripRepository;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AppShellFxmlTest {
    @BeforeAll
    static void initializeJavaFx() {
        Platform.startup(() -> { });
    }

    @AfterAll
    static void shutDownJavaFx() {
        Platform.exit();
    }

    @Test
    void loadAppShell_fxmlResourcesAndControllers_createsRootScene() throws IOException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/doggo/ui/javafx/AppShell.fxml"));
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
            throw new IllegalArgumentException("Unsupported FXML controller: " + type.getName());
        });

        Parent root = loader.load();

        assertInstanceOf(BorderPane.class, root);
        assertInstanceOf(AppShellController.class, loader.getController());
        assertFalse(root.getStylesheets().isEmpty());
        assertNotNull(getClass().getResource("/doggo/ui/javafx/DashboardView.fxml"));
        assertNotNull(getClass().getResource("/doggo/ui/javafx/OrganiseView.fxml"));
    }
}
