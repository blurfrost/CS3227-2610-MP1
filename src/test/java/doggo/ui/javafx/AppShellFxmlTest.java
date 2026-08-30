package doggo.ui.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Trip;
import doggo.storage.InMemoryTripRepository;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
            if (type == GalleryController.class) {
                return new GalleryController(service);
            }
            throw new IllegalArgumentException("Unsupported FXML controller: " + type.getName());
        });

        Parent root = loader.load();

        assertInstanceOf(BorderPane.class, root);
        assertInstanceOf(AppShellController.class, loader.getController());
        assertFalse(root.getStylesheets().isEmpty());
        assertInstanceOf(Button.class, loader.getNamespace().get("newTripButton"));
        assertNotNull(getClass().getResource("/doggo/ui/javafx/DashboardView.fxml"));
        assertNotNull(getClass().getResource("/doggo/ui/javafx/OrganiseView.fxml"));
        assertNotNull(getClass().getResource("/doggo/ui/javafx/GalleryView.fxml"));
    }

    @Test
    void updatePlanCell_planWithYear_rendersFullDateAndTime() {
        Plan plan = new Plan(UUID.randomUUID(), "Night market", LocalDate.of(2026, 8, 30),
                LocalTime.of(23, 30));
        PlanCell cell = new PlanCell();

        cell.updateItem(plan, false);

        VBox card = assertInstanceOf(VBox.class, cell.getGraphic());
        HBox dateAndTime = assertInstanceOf(HBox.class, card.getChildren().getFirst());
        Label dateLabel = assertInstanceOf(Label.class, dateAndTime.getChildren().getFirst());
        Label timeLabel = assertInstanceOf(Label.class, dateAndTime.getChildren().getLast());
        assertEquals("30 Aug 2026 (Sun)", dateLabel.getText());
        assertEquals("23:30", timeLabel.getText());
    }

    @Test
    void handleDashboard_afterPlanAdded_refreshesDisplayedPlans() throws IOException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));
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
            if (type == GalleryController.class) {
                return new GalleryController(service);
            }
            throw new IllegalArgumentException("Unsupported FXML controller: " + type.getName());
        });
        loader.load();

        VBox dashboardPage = assertInstanceOf(VBox.class, loader.getNamespace().get("dashboardPage"));
        ListView<?> planList = assertInstanceOf(ListView.class, dashboardPage.lookup("#planList"));
        assertEquals(0, planList.getItems().size());

        service.addPlan(trip.id(), "Night market", LocalDate.of(2027, 1, 5), LocalTime.of(23, 30));
        Button dashboardButton = assertInstanceOf(Button.class, loader.getNamespace().get("dashboardButton"));
        dashboardButton.fire();

        assertEquals(1, planList.getItems().size());
    }

    @Test
    void createPlanDialog_defaultDate_usesCurrentServiceDate()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        FutureTask<LocalDate> dateTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            PlanCreationDialog dialog = new PlanCreationDialog(service, trip, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            GridPane fields = assertInstanceOf(GridPane.class, form.getChildren().get(1));
            DatePicker datePicker = assertInstanceOf(DatePicker.class, fields.getChildren().get(3));
            return datePicker.getValue();
        });
        Platform.runLater(dateTask);

        assertEquals(service.getCurrentDate(), dateTask.get(5, TimeUnit.SECONDS));
    }
}
