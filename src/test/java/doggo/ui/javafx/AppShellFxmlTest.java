package doggo.ui.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import doggo.TestClock;
import doggo.application.DashboardEntry;
import doggo.application.DoggoService;
import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;
import doggo.storage.InMemoryTripRepository;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
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
        FXMLLoader loader = createAppShellLoader(service);

        Parent root = loader.load();

        assertInstanceOf(BorderPane.class, root);
        assertInstanceOf(AppShellController.class, loader.getController());
        assertFalse(root.getStylesheets().isEmpty());
        assertInstanceOf(Button.class, loader.getNamespace().get("newTripButton"));
        assertInstanceOf(Button.class, root.lookup("#editTripButton"));
        assertInstanceOf(Button.class, root.lookup("#editPlanButton"));
        assertNotNull(getClass().getResource("/doggo/ui/javafx/DashboardView.fxml"));
        assertNotNull(getClass().getResource("/doggo/ui/javafx/OrganiseView.fxml"));
        assertNotNull(getClass().getResource("/doggo/ui/javafx/GalleryView.fxml"));
    }

    @Test
    void loadMainViews_longTitles_wrapWithinDetailPanel() throws IOException {
        String longTitle = "W".repeat(Trip.MAX_TITLE_LENGTH);
        double[] pageWidths = {920, 722};

        for (double pageWidth : pageWidths) {
            DoggoService dashboardService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            Trip dashboardTrip = dashboardService.createTrip(longTitle, LocalDate.of(2027, 1, 5),
                    LocalDate.of(2027, 1, 5));
            dashboardService.addPlan(dashboardTrip.id(), longTitle, LocalDate.of(2027, 1, 5),
                    LocalTime.of(9, 0));
            assertDetailLabelsWrap(loadView("DashboardView.fxml", dashboardService),
                    List.of("#detailDestinationLabel", "#detailTripLabel"), longTitle, pageWidth);

            DoggoService organiseService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            organiseService.createTrip(longTitle, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));
            assertDetailLabelsWrap(loadView("OrganiseView.fxml", organiseService),
                    List.of("#detailTitleLabel"), longTitle, pageWidth);

            DoggoService galleryService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            galleryService.createTrip(longTitle, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 1));
            assertDetailLabelsWrap(loadView("GalleryView.fxml", galleryService),
                    List.of("#detailTitleLabel"), longTitle, pageWidth);
        }
    }

    @Test
    void loadMainViews_atSupportedWidths_useEqualPanelColumns() throws IOException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        double[] windowWidths = {1180, 960};

        for (double windowWidth : windowWidths) {
            double pageWidth = Math.min(920, windowWidth - 238);
            assertEqualPanelColumns(loadView("DashboardView.fxml", service), pageWidth);
            assertEqualPanelColumns(loadView("OrganiseView.fxml", service), pageWidth);
            assertEqualPanelColumns(loadView("GalleryView.fxml", service), pageWidth);
        }
    }

    @Test
    void appShell_supportedSizes_preserveLayoutAcrossNavigationAndSelection() throws IOException {
        String title = "W".repeat(Trip.MAX_TITLE_LENGTH);
        String destination = "W".repeat(Plan.MAX_DESTINATION_LENGTH);
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip currentTrip = service.createTrip(title, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));
        service.addPlan(currentTrip.id(), destination, LocalDate.of(2027, 1, 5), LocalTime.of(9, 0));
        Trip pastTrip = service.createTrip(title, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 1));
        service.addPlan(pastTrip.id(), destination, LocalDate.of(2027, 1, 1), LocalTime.of(9, 0));

        FXMLLoader loader = createAppShellLoader(service);
        BorderPane root = assertInstanceOf(BorderPane.class, loader.load());
        VBox dashboardPage = assertInstanceOf(VBox.class, loader.getNamespace().get("dashboardPage"));
        VBox organisePage = assertInstanceOf(VBox.class, loader.getNamespace().get("organisePage"));
        VBox galleryPage = assertInstanceOf(VBox.class, loader.getNamespace().get("galleryPage"));
        Button dashboardButton = assertInstanceOf(Button.class, loader.getNamespace().get("dashboardButton"));
        Button organiseButton = assertInstanceOf(Button.class, loader.getNamespace().get("organiseButton"));
        Button galleryButton = assertInstanceOf(Button.class, loader.getNamespace().get("galleryButton"));

        Scene scene = new Scene(root, 1180, 760);
        scene.getRoot().applyCss();
        for (double[] windowSize : new double[][] {{1180, 760}, {960, 640}}) {
            scene.getRoot().resize(windowSize[0], windowSize[1]);
            dashboardButton.fire();
            root.layout();
            root.layout();
            assertRenderedEqualPanelColumns(dashboardPage);
            assertRenderedDetailLabelsWrap(dashboardPage,
                    List.of("#detailDestinationLabel", "#detailTripLabel"), destination);
            assertRenderedDashboardPlanCardFitsViewport(dashboardPage, destination);
            assertInstanceOf(ListView.class, dashboardPage.lookup("#planList"))
                    .getSelectionModel().select(0);
            assertRenderedEqualPanelColumns(dashboardPage);

            organiseButton.fire();
            root.layout();
            root.layout();
            assertRenderedEqualPanelColumns(organisePage);
            assertRenderedDetailLabelsWrap(organisePage, List.of("#detailTitleLabel"), title);
            assertRenderedTripCardFitsViewport(organisePage, title);
            assertRenderedPlanCardFitsViewport(organisePage, destination);
            assertInstanceOf(ListView.class, organisePage.lookup("#tripList"))
                    .getSelectionModel().select(0);
            assertRenderedEqualPanelColumns(organisePage);

            galleryButton.fire();
            root.layout();
            root.layout();
            assertRenderedEqualPanelColumns(galleryPage);
            assertRenderedDetailLabelsWrap(galleryPage, List.of("#detailTitleLabel"), title);
            assertRenderedTripCardFitsViewport(galleryPage, title);
            assertRenderedPlanCardFitsViewport(galleryPage, destination);
            assertInstanceOf(ListView.class, galleryPage.lookup("#tripList"))
                    .getSelectionModel().select(0);
            assertRenderedEqualPanelColumns(galleryPage);
        }
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
        Label destinationLabel = assertInstanceOf(Label.class, details.getChildren().getLast());
        assertEquals("30 Aug 2026 (Sun)", dateLabel.getText());
        assertEquals("23:30", timeLabel.getText());
        assertEquals(plan.destination(), destinationLabel.getTooltip().getText());
        assertFalse(destinationLabel.isWrapText());
        assertEquals(OverrunStyle.ELLIPSIS, destinationLabel.getTextOverrun());
        assertEquals("Edit", assertInstanceOf(Button.class, card.getChildren().getLast()).getText());
    }

    @Test
    void updateTripCell_longTitle_usesEllipsisWithoutStatusBadge() {
        String title = "a".repeat(Trip.MAX_TITLE_LENGTH);
        Trip trip = new Trip(UUID.randomUUID(), title, LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));
        TripCell cell = new TripCell();

        cell.updateItem(trip, false);

        VBox card = assertInstanceOf(VBox.class, cell.getGraphic());
        HBox header = assertInstanceOf(HBox.class, card.getChildren().getFirst());
        Label titleLabel = assertInstanceOf(Label.class, header.getChildren().getFirst());
        assertEquals(1, header.getChildren().size());
        assertEquals(title, titleLabel.getTooltip().getText());
        assertFalse(titleLabel.isWrapText());
        assertEquals(OverrunStyle.ELLIPSIS, titleLabel.getTextOverrun());
    }

    @Test
    void loadTripViews_longTitles_ellipsizeWithinViewport() throws IOException {
        String title = "W".repeat(Trip.MAX_TITLE_LENGTH);
        double pageWidth = 722;

        DoggoService organiseService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        organiseService.createTrip(title, LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));
        assertTripCardFitsViewport(loadView("OrganiseView.fxml", organiseService), title, pageWidth);

        DoggoService galleryService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        galleryService.createTrip(title, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 1));
        assertTripCardFitsViewport(loadView("GalleryView.fxml", galleryService), title, pageWidth);
    }

    @Test
    void loadTripViews_moreTrips_shrinkCardsForVerticalScrollbar() throws IOException {
        for (double pageWidth : new double[] {920, 722}) {
            assertTripListAdjustsForVerticalScrollbar(
                    "OrganiseView.fxml", LocalDate.of(2027, 1, 5), pageWidth);
            assertTripListAdjustsForVerticalScrollbar(
                    "GalleryView.fxml", LocalDate.of(2027, 1, 1), pageWidth);
        }
    }

    @Test
    void loadMainViews_longPlanNames_ellipsizeWithinViewport() throws IOException {
        String destination = "W".repeat(Plan.MAX_DESTINATION_LENGTH);
        double pageWidth = 722;

        DoggoService dashboardService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip dashboardTrip = dashboardService.createTrip("Japan", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));
        dashboardService.addPlan(dashboardTrip.id(), destination, LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        assertDashboardPlanCardFitsViewport(loadView("DashboardView.fxml", dashboardService), destination, pageWidth);

        DoggoService organiseService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip organiseTrip = organiseService.createTrip("Japan", LocalDate.of(2027, 1, 5),
                LocalDate.of(2027, 1, 5));
        organiseService.addPlan(organiseTrip.id(), destination, LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        assertPlanCardFitsViewport(loadView("OrganiseView.fxml", organiseService), destination, pageWidth);

        DoggoService galleryService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip galleryTrip = galleryService.createTrip("Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 1));
        galleryService.addPlan(galleryTrip.id(), destination, LocalDate.of(2027, 1, 1),
                LocalTime.of(9, 0));
        assertPlanCardFitsViewport(loadView("GalleryView.fxml", galleryService), destination, pageWidth);
    }

    @Test
    void loadMainViews_manyPlans_useVerticalScrollingOnly() throws IOException {
        String destination = "W".repeat(Plan.MAX_DESTINATION_LENGTH);
        for (double pageWidth : new double[] {920, 722}) {
            DoggoService dashboardService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            Trip dashboardTrip = dashboardService.createTrip("Japan", LocalDate.of(2027, 1, 5),
                    LocalDate.of(2027, 1, 5));
            addPlans(dashboardService, dashboardTrip, destination, LocalDate.of(2027, 1, 5));
            assertListUsesVerticalScrollingOnly(loadView("DashboardView.fxml", dashboardService), pageWidth);

            DoggoService organiseService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            Trip organiseTrip = organiseService.createTrip("Japan", LocalDate.of(2027, 1, 5),
                    LocalDate.of(2027, 1, 5));
            addPlans(organiseService, organiseTrip, destination, LocalDate.of(2027, 1, 5));
            assertListUsesVerticalScrollingOnly(loadView("OrganiseView.fxml", organiseService), pageWidth);

            DoggoService galleryService = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            Trip galleryTrip = galleryService.createTrip("Japan", LocalDate.of(2027, 1, 1),
                    LocalDate.of(2027, 1, 1));
            addPlans(galleryService, galleryTrip, destination, LocalDate.of(2027, 1, 1));
            assertListUsesVerticalScrollingOnly(loadView("GalleryView.fxml", galleryService), pageWidth);
        }
    }

    @Test
    void updateDashboardEntryCell_names_useEllipsisAndTooltips() {
        String destination = "a".repeat(Plan.MAX_DESTINATION_LENGTH);
        String tripTitle = "b".repeat(Trip.MAX_TITLE_LENGTH);
        Plan plan = new Plan(UUID.randomUUID(), destination, LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        DashboardEntry entry = new DashboardEntry(UUID.randomUUID(), tripTitle, plan);
        DashboardEntryCell cell = new DashboardEntryCell();

        cell.updateItem(entry, false);

        VBox card = assertInstanceOf(VBox.class, cell.getGraphic());
        Label destinationLabel = assertInstanceOf(Label.class, card.getChildren().get(1));
        Label tripLabel = assertInstanceOf(Label.class, card.getChildren().get(2));
        assertEquals(destination, destinationLabel.getTooltip().getText());
        assertEquals(tripTitle, tripLabel.getTooltip().getText());
        assertFalse(destinationLabel.isWrapText());
        assertFalse(tripLabel.isWrapText());
        assertEquals(OverrunStyle.ELLIPSIS, destinationLabel.getTextOverrun());
        assertEquals(OverrunStyle.ELLIPSIS, tripLabel.getTextOverrun());
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
    void createPlanDialog_currentDateWithinTrip_usesCurrentServiceDate()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));

        assertEquals(service.getCurrentDate(), getPlanCreationDate(service, trip));
    }

    @Test
    void createPlanDialog_currentDateBeforeTrip_usesTripStartDate()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 6),
                LocalDate.of(2027, 1, 9));

        assertEquals(trip.startDate(), getPlanCreationDate(service, trip));
    }

    @Test
    void createPlanDialog_currentDateAfterTrip_usesTripStartDate()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));

        assertEquals(trip.startDate(), getPlanCreationDate(service, trip));
    }

    @Test
    void creationDialogs_areResizable() throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = new Plan(UUID.randomUUID(), "Senso-ji Temple", LocalDate.of(2027, 1, 6),
                LocalTime.of(9, 30));
        FutureTask<Boolean> dialogTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            TripCreationDialog tripDialog = new TripCreationDialog(service, owner);
            PlanCreationDialog createDialog = new PlanCreationDialog(service, trip, owner);
            PlanCreationDialog editDialog = new PlanCreationDialog(service, trip, plan, owner);
            return tripDialog.isResizable() && createDialog.isResizable() && editDialog.isResizable();
        });
        Platform.runLater(dialogTask);

        assertTrue(dialogTask.get(5, TimeUnit.SECONDS));
    }

    @Test
    void dialogWindowSupport_setsOpeningDimensionsAsMinimums()
            throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<DialogSize> dialogTask = new FutureTask<>(() -> {
            Stage stage = new Stage();
            stage.setWidth(480);
            stage.setHeight(320);
            DialogWindowSupport.setOpeningSizeAsMinimum(stage);
            return new DialogSize(stage.getWidth(), stage.getHeight(), stage.getMinWidth(),
                    stage.getMinHeight());
        });
        Platform.runLater(dialogTask);

        DialogSize size = dialogTask.get(5, TimeUnit.SECONDS);
        assertEquals(size.width(), size.minWidth(), 0.01);
        assertEquals(size.height(), size.minHeight(), 0.01);
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

    @Test
    void editTripDialog_prefillsTripFieldsAndUsesSaveAction()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        FutureTask<TripEditDialogState> dialogTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            TripCreationDialog dialog = new TripCreationDialog(service, trip, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            GridPane fields = assertInstanceOf(GridPane.class, form.getChildren().get(1));
            TextField titleField = assertInstanceOf(TextField.class, fields.getChildren().get(1));
            DatePicker startDatePicker = assertInstanceOf(DatePicker.class, fields.getChildren().get(3));
            DatePicker endDatePicker = assertInstanceOf(DatePicker.class, fields.getChildren().get(5));
            Button submitButton = assertInstanceOf(Button.class,
                    dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().getFirst()));
            return new TripEditDialogState(dialog.getTitle(), titleField.getText(), startDatePicker.getValue(),
                    endDatePicker.getValue(), submitButton.getText());
        });
        Platform.runLater(dialogTask);

        TripEditDialogState state = dialogTask.get(5, TimeUnit.SECONDS);
        assertEquals("Edit trip", state.title());
        assertEquals(trip.title(), state.tripTitle());
        assertEquals(trip.startDate(), state.startDate());
        assertEquals(trip.endDate(), state.endDate());
        assertEquals("Save changes", state.submitText());
    }

    @Test
    void editTripDialog_submit_persistsUpdatedTripAndPreservesReview()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        service.setTripReview(trip.id(), new Review(OptionalInt.of(5), Optional.empty()));
        FutureTask<Trip> editTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            TripCreationDialog dialog = new TripCreationDialog(service, trip, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            GridPane fields = assertInstanceOf(GridPane.class, form.getChildren().get(1));
            TextField titleField = assertInstanceOf(TextField.class, fields.getChildren().get(1));
            DatePicker startDatePicker = assertInstanceOf(DatePicker.class, fields.getChildren().get(3));
            DatePicker endDatePicker = assertInstanceOf(DatePicker.class, fields.getChildren().get(5));
            titleField.setText("Korea");
            startDatePicker.setValue(LocalDate.of(2027, 1, 2));
            endDatePicker.setValue(LocalDate.of(2027, 1, 10));
            Button submitButton = assertInstanceOf(Button.class,
                    dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().getFirst()));
            submitButton.fire();
            return service.getTrip(trip.id()).orElseThrow();
        });
        Platform.runLater(editTask);

        Trip updatedTrip = editTask.get(5, TimeUnit.SECONDS);
        assertEquals(trip.id(), updatedTrip.id());
        assertEquals("Korea", updatedTrip.title());
        assertEquals(LocalDate.of(2027, 1, 2), updatedTrip.startDate());
        assertEquals(LocalDate.of(2027, 1, 10), updatedTrip.endDate());
        assertEquals(OptionalInt.of(5), updatedTrip.review().orElseThrow().rating());
    }

    private record TripEditDialogState(String title, String tripTitle, LocalDate startDate,
                                       LocalDate endDate, String submitText) {
    }

    private record EditDialogState(String title, String destination, LocalDate date,
                                   String time, String submitText) {
    }

    private record DialogSize(double width, double height, double minWidth, double minHeight) {
    }

    private static FXMLLoader createAppShellLoader(DoggoService service) {
        FXMLLoader loader = new FXMLLoader(AppShellFxmlTest.class.getResource("/doggo/ui/javafx/AppShell.fxml"));
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
        return loader;
    }

    private static VBox loadView(String resourceName, DoggoService service) throws IOException {
        FXMLLoader loader = createViewLoader(resourceName, service);
        return assertInstanceOf(VBox.class, loader.load());
    }

    private static FXMLLoader createViewLoader(String resourceName, DoggoService service) {
        FXMLLoader loader = new FXMLLoader(AppShellFxmlTest.class.getResource(
                "/doggo/ui/javafx/" + resourceName));
        loader.setControllerFactory(type -> {
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
        return loader;
    }

    private static void assertEqualPanelColumns(VBox page, double pageWidth) {
        page.resize(pageWidth, 600);
        Scene scene = new Scene(page, pageWidth, 600);
        scene.getRoot().applyCss();
        page.layout();

        assertRenderedEqualPanelColumns(page);
    }

    private static void assertRenderedEqualPanelColumns(VBox page) {
        GridPane panels = findMainLayout(page);
        assertEquals(18.0, panels.getHgap(), 0.01);
        assertEquals(2, panels.getColumnConstraints().size());
        assertEquals(50.0, panels.getColumnConstraints().getFirst().getPercentWidth(), 0.01);
        assertEquals(50.0, panels.getColumnConstraints().getLast().getPercentWidth(), 0.01);
        assertEquals(1, panels.getRowConstraints().size());

        ColumnConstraints firstColumn = panels.getColumnConstraints().getFirst();
        ColumnConstraints secondColumn = panels.getColumnConstraints().getLast();
        assertEquals(0.0, firstColumn.getMinWidth(), 0.01);
        assertEquals(0.0, secondColumn.getMinWidth(), 0.01);
        assertEquals(Priority.ALWAYS, panels.getRowConstraints().getFirst().getVgrow());

        VBox firstPanel = assertInstanceOf(VBox.class, panels.getChildren().getFirst());
        VBox secondPanel = assertInstanceOf(VBox.class, panels.getChildren().getLast());
        double expectedPanelWidth = (panels.getWidth() - panels.getHgap()) / 2;
        assertEquals(expectedPanelWidth, firstPanel.getWidth(), 0.01);
        assertEquals(expectedPanelWidth, secondPanel.getWidth(), 0.01);
    }

    private static void assertDetailLabelsWrap(VBox page, List<String> labelIds, String expectedText,
                                                double pageWidth) {
        page.resize(pageWidth, 600);
        Scene scene = new Scene(page, pageWidth, 600);
        scene.getRoot().applyCss();
        page.layout();

        assertRenderedDetailLabelsWrap(page, labelIds, expectedText);
    }

    private static void assertRenderedDetailLabelsWrap(VBox page, List<String> labelIds, String expectedText) {
        GridPane panels = findMainLayout(page);
        VBox detailPanel = assertInstanceOf(VBox.class, panels.getChildren().getLast());
        double expectedPanelWidth = (panels.getWidth() - panels.getHgap()) / 2;
        assertEquals(expectedPanelWidth, panels.getChildren().getFirst().getBoundsInParent().getWidth(), 0.01);
        assertEquals(expectedPanelWidth, detailPanel.getWidth(), 0.01);
        labelIds.forEach(labelId -> {
            TextFlow label = assertInstanceOf(TextFlow.class, page.lookup(labelId));
            StringBuilder displayedText = new StringBuilder();
            label.getChildren().forEach(child ->
                    displayedText.append(assertInstanceOf(Text.class, child).getText()));
            String expectedLabelText = labelId.equals("#detailTripLabel")
                    ? "Trip · " + expectedText
                    : expectedText;
            assertEquals(expectedLabelText, displayedText.toString());
            Text firstCharacter = assertInstanceOf(Text.class, label.getChildren().getFirst());
            assertTrue(label.getHeight() > firstCharacter.getLayoutBounds().getHeight() * 1.5,
                    () -> labelId + " height=" + label.getHeight() + " lineHeight="
                            + firstCharacter.getLayoutBounds().getHeight());
            assertTrue(label.getWidth() <= detailPanel.getWidth());
        });
    }

    private static void assertTripCardFitsViewport(VBox page, String expectedTitle, double pageWidth) {
        page.resize(pageWidth, 600);
        Scene scene = new Scene(page, pageWidth, 600);
        scene.getRoot().applyCss();
        page.layout();
        page.layout();

        assertRenderedTripCardFitsViewport(page, expectedTitle);
    }

    private static void assertRenderedTripCardFitsViewport(VBox page, String expectedTitle) {
        ListView<?> tripList = assertInstanceOf(ListView.class, page.lookup("#tripList"));
        ListCell<?> cell = assertInstanceOf(ListCell.class, tripList.lookup(".list-cell"));
        VBox card = assertInstanceOf(VBox.class, cell.getGraphic());
        HBox header = assertInstanceOf(HBox.class, card.getChildren().getFirst());
        Label titleLabel = assertInstanceOf(Label.class, header.getChildren().getFirst());
        assertEquals(1, header.getChildren().size());
        assertEquals(expectedTitle, titleLabel.getTooltip().getText());
        assertTrue(titleLabel.isTextTruncated());
        assertTrue(card.getWidth() <= tripList.getWidth());
        assertHorizontalScrollbarHidden(tripList);
    }

    private static void assertDashboardPlanCardFitsViewport(VBox page, String expectedDestination, double pageWidth) {
        page.resize(pageWidth, 600);
        Scene scene = new Scene(page, pageWidth, 600);
        scene.getRoot().applyCss();
        page.layout();
        page.layout();

        assertRenderedDashboardPlanCardFitsViewport(page, expectedDestination);
    }

    private static void assertRenderedDashboardPlanCardFitsViewport(VBox page, String expectedDestination) {
        ListView<?> planList = assertInstanceOf(ListView.class, page.lookup("#planList"));
        ListCell<?> cell = assertInstanceOf(ListCell.class, planList.lookup(".list-cell"));
        VBox card = assertInstanceOf(VBox.class, cell.getGraphic());
        Label destinationLabel = assertInstanceOf(Label.class, card.getChildren().get(1));
        assertEquals(expectedDestination, destinationLabel.getTooltip().getText());
        assertTrue(destinationLabel.isTextTruncated());
        assertTrue(card.getWidth() <= planList.getWidth(),
                () -> "cardWidth=" + card.getWidth() + ", listWidth=" + planList.getWidth()
                        + ", cellWidth=" + cell.getWidth() + ", pageWidth=" + page.getWidth());
        assertHorizontalScrollbarHidden(planList);
    }

    private static void assertPlanCardFitsViewport(VBox page, String expectedDestination, double pageWidth) {
        page.resize(pageWidth, 600);
        Scene scene = new Scene(page, pageWidth, 600);
        scene.getRoot().applyCss();
        page.layout();
        page.layout();

        assertRenderedPlanCardFitsViewport(page, expectedDestination);
    }

    private static void assertRenderedPlanCardFitsViewport(VBox page, String expectedDestination) {
        ListView<?> planList = assertInstanceOf(ListView.class, page.lookup("#planList"));
        ListCell<?> cell = assertInstanceOf(ListCell.class, planList.lookup(".list-cell"));
        HBox card = assertInstanceOf(HBox.class, cell.getGraphic());
        VBox details = assertInstanceOf(VBox.class, card.getChildren().getFirst());
        Label destinationLabel = assertInstanceOf(Label.class, details.getChildren().getLast());
        assertEquals(expectedDestination, destinationLabel.getTooltip().getText());
        assertTrue(destinationLabel.isTextTruncated());
        assertEquals("Edit", assertInstanceOf(Button.class, card.getChildren().getLast()).getText());
        assertTrue(card.getWidth() <= planList.getWidth(),
                () -> "cardWidth=" + card.getWidth() + ", listWidth=" + planList.getWidth()
                        + ", cellWidth=" + cell.getWidth() + ", pageWidth=" + page.getWidth());
        assertHorizontalScrollbarHidden(planList);
    }

    private static void assertHorizontalScrollbarHidden(ListView<?> listView) {
        ScrollBar horizontalScrollBar = findScrollBar(listView, Orientation.HORIZONTAL);
        assertFalse(horizontalScrollBar.isVisible(), () -> "Horizontal scrollbar should remain hidden. listWidth="
                + listView.getWidth() + ", flow=" + listView.lookup(".virtual-flow")
                + ", barWidth=" + horizontalScrollBar.getWidth());
    }

    private static ScrollBar findScrollBar(ListView<?> listView, Orientation orientation) {
        return listView.lookupAll(".scroll-bar").stream()
                .filter(ScrollBar.class::isInstance)
                .map(ScrollBar.class::cast)
                .filter(scrollBar -> scrollBar.getOrientation() == orientation)
                .findFirst()
                .orElseThrow();
    }

    private static void assertTripListAdjustsForVerticalScrollbar(
            String resourceName, LocalDate tripDate, double pageWidth)
            throws IOException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        String title = "W".repeat(Trip.MAX_TITLE_LENGTH);
        service.createTrip(title, tripDate, tripDate);
        FXMLLoader loader = createViewLoader(resourceName, service);
        VBox page = assertInstanceOf(VBox.class, loader.load());
        Scene scene = new Scene(page, pageWidth, 600);
        scene.getRoot().applyCss();
        page.layout();
        page.layout();

        ListView<?> tripList = assertInstanceOf(ListView.class, page.lookup("#tripList"));
        ScrollBar verticalScrollBar = findScrollBar(tripList, Orientation.VERTICAL);
        assertFalse(verticalScrollBar.isVisible());
        Region initialCard = getFirstCard(tripList);
        double initialCardWidth = initialCard.getWidth();

        for (int index = 1; index < 12; index++) {
            service.createTrip(title, tripDate, tripDate);
        }
        refreshTripView(loader.getController());
        scene.getRoot().applyCss();
        page.layout();
        page.layout();

        assertTrue(verticalScrollBar.isVisible());
        Region overflowCard = getFirstCard(tripList);
        assertTrue(overflowCard.getWidth() < initialCardWidth);
        assertFitsBesideVerticalScrollbar(tripList, overflowCard, verticalScrollBar);
        assertHorizontalScrollbarHidden(tripList);
    }

    private static void assertListUsesVerticalScrollingOnly(VBox page, double pageWidth) {
        page.resize(pageWidth, 600);
        Scene scene = new Scene(page, pageWidth, 600);
        scene.getRoot().applyCss();
        page.layout();
        page.layout();

        ListView<?> planList = assertInstanceOf(ListView.class, page.lookup("#planList"));
        ScrollBar verticalScrollBar = findScrollBar(planList, Orientation.VERTICAL);
        assertTrue(verticalScrollBar.isVisible());
        assertFitsBesideVerticalScrollbar(planList, getFirstCard(planList), verticalScrollBar);
        assertHorizontalScrollbarHidden(planList);
    }

    private static Region getFirstCard(ListView<?> listView) {
        ListCell<?> cell = assertInstanceOf(ListCell.class, listView.lookup(".list-cell"));
        return assertInstanceOf(Region.class, cell.getGraphic());
    }

    private static void assertFitsBesideVerticalScrollbar(
            ListView<?> listView, Region card, ScrollBar verticalScrollBar) {
        double usableWidth = listView.getWidth() - listView.getInsets().getLeft()
                - listView.getInsets().getRight() - verticalScrollBar.getWidth();
        assertTrue(card.getWidth() <= usableWidth,
                () -> "cardWidth=" + card.getWidth() + ", usableWidth=" + usableWidth);
    }

    private static void refreshTripView(Object controller) {
        if (controller instanceof OrganiseController organiseController) {
            organiseController.refresh();
            return;
        }
        assertInstanceOf(GalleryController.class, controller).refresh();
    }

    private static void addPlans(DoggoService service, Trip trip, String destination, LocalDate date) {
        for (int index = 0; index < 12; index++) {
            service.addPlan(trip.id(), destination, date, LocalTime.of(9, index));
        }
    }

    private static GridPane findMainLayout(VBox page) {
        return assertInstanceOf(GridPane.class, page.getChildren().stream()
                .filter(GridPane.class::isInstance)
                .findFirst()
                .orElseThrow());
    }

    private static LocalDate getPlanCreationDate(DoggoService service, Trip trip)
            throws InterruptedException, ExecutionException, TimeoutException {
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
        return dateTask.get(5, TimeUnit.SECONDS);
    }

}
