package doggo.ui.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Review;
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
import javafx.scene.control.TextField;
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
        assertInstanceOf(Button.class, root.lookup("#editPlanButton"));
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

        HBox card = assertInstanceOf(HBox.class, cell.getGraphic());
        VBox details = assertInstanceOf(VBox.class, card.getChildren().getFirst());
        HBox dateAndTime = assertInstanceOf(HBox.class, details.getChildren().getFirst());
        Label dateLabel = assertInstanceOf(Label.class, dateAndTime.getChildren().getFirst());
        Label timeLabel = assertInstanceOf(Label.class, dateAndTime.getChildren().getLast());
        assertEquals("30 Aug 2026 (Sun)", dateLabel.getText());
        assertEquals("23:30", timeLabel.getText());
        assertEquals("Edit", assertInstanceOf(Button.class, card.getChildren().getLast()).getText());
    }

    @Test
    void editPlanCell_editButton_invokesHandlerWithRenderedPlan() {
        Plan plan = new Plan(UUID.randomUUID(), "Night market", LocalDate.of(2026, 8, 30),
                LocalTime.of(23, 30));
        AtomicReference<Plan> editedPlan = new AtomicReference<>();
        PlanCell cell = new PlanCell(editedPlan::set);

        cell.updateItem(plan, false);

        Button editButton = assertInstanceOf(Button.class,
                assertInstanceOf(HBox.class, cell.getGraphic()).getChildren().getLast());
        editButton.fire();

        assertSame(plan, editedPlan.get());
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

    @Test
    void editPlanDialog_prefillsPlanFieldsAndUsesSaveAction()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = new Plan(UUID.randomUUID(), "Senso-ji Temple", LocalDate.of(2027, 1, 6),
                LocalTime.of(9, 30));
        FutureTask<EditDialogState> dialogTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            PlanCreationDialog dialog = new PlanCreationDialog(service, trip, plan, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            GridPane fields = assertInstanceOf(GridPane.class, form.getChildren().get(1));
            TextField destinationField = assertInstanceOf(TextField.class, fields.getChildren().get(1));
            DatePicker datePicker = assertInstanceOf(DatePicker.class, fields.getChildren().get(3));
            TextField timeField = assertInstanceOf(TextField.class, fields.getChildren().get(5));
            Button submitButton = assertInstanceOf(Button.class,
                    dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().getFirst()));
            return new EditDialogState(dialog.getTitle(), destinationField.getText(), datePicker.getValue(),
                    timeField.getText(), submitButton.getText());
        });
        Platform.runLater(dialogTask);

        EditDialogState state = dialogTask.get(5, TimeUnit.SECONDS);
        assertEquals("Edit plan", state.title());
        assertEquals(plan.destination(), state.destination());
        assertEquals(plan.date(), state.date());
        assertEquals("09:30", state.time());
        assertEquals("Save changes", state.submitText());
    }

    @Test
    void editPlanDialog_submit_persistsUpdatedPlan()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Senso-ji Temple", LocalDate.of(2027, 1, 5),
                LocalTime.MIDNIGHT);
        Review review = new Review(OptionalInt.of(4), Optional.empty());
        service.setPlanReview(trip.id(), plan.id(), review);
        FutureTask<Plan> editTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            PlanCreationDialog dialog = new PlanCreationDialog(service, trip, plan, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            GridPane fields = assertInstanceOf(GridPane.class, form.getChildren().get(1));
            TextField destinationField = assertInstanceOf(TextField.class, fields.getChildren().get(1));
            DatePicker datePicker = assertInstanceOf(DatePicker.class, fields.getChildren().get(3));
            TextField timeField = assertInstanceOf(TextField.class, fields.getChildren().get(5));
            destinationField.setText("Tokyo Tower");
            datePicker.setValue(LocalDate.of(2027, 1, 7));
            timeField.setText("10:15");
            Button submitButton = assertInstanceOf(Button.class,
                    dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().getFirst()));
            submitButton.fire();
            return service.getTrip(trip.id()).orElseThrow().plans().getFirst();
        });
        Platform.runLater(editTask);

        Plan updatedPlan = editTask.get(5, TimeUnit.SECONDS);
        assertEquals(plan.id(), updatedPlan.id());
        assertEquals("Tokyo Tower", updatedPlan.destination());
        assertEquals(LocalDate.of(2027, 1, 7), updatedPlan.date());
        assertEquals(LocalTime.of(10, 15), updatedPlan.time());
        assertEquals(OptionalInt.of(4), updatedPlan.review().orElseThrow().rating());
    }

    private record EditDialogState(String title, String destination, LocalDate date,
                                   String time, String submitText) {
    }
}
