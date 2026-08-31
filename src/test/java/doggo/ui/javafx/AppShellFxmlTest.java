package doggo.ui.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
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
        Platform.startup(() -> Platform.setImplicitExit(false));
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
        assertInstanceOf(Button.class, root.lookup("#reviewTripButton"));
        assertInstanceOf(Button.class, root.lookup("#deleteTripButton"));
        assertInstanceOf(Button.class, root.lookup("#editPlanButton"));
        assertInstanceOf(Button.class, root.lookup("#reviewPlanButton"));
        assertInstanceOf(Button.class, root.lookup("#deletePlanButton"));
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
    void loadMainViews_atMinimumWidth_useEqualPanelColumns() throws IOException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        double windowWidth = 1180;

        double pageWidth = Math.min(920, windowWidth - 238);
        assertEqualPanelColumns(loadView("DashboardView.fxml", service), pageWidth);
        assertEqualPanelColumns(loadView("OrganiseView.fxml", service), pageWidth);
        assertEqualPanelColumns(loadView("GalleryView.fxml", service), pageWidth);
    }

    @Test
    void loadMainViews_headerStructure_matchesDashboardSpacing() throws IOException {
        for (String resourceName : List.of("DashboardView.fxml", "OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            VBox page = loadView(resourceName, service);

            assertEquals(10.0, page.getSpacing(), 0.01);
            assertInstanceOf(Label.class, page.getChildren().get(0));
            assertInstanceOf(Label.class, page.getChildren().get(1));
            assertInstanceOf(Label.class, page.getChildren().get(2));
            assertTrue(page.getChildren().get(0).getStyleClass().contains("eyebrow"));
            assertTrue(page.getChildren().get(1).getStyleClass().contains("page-title"));
            assertTrue(page.getChildren().get(2).getStyleClass().contains("page-subtitle"));
            assertNull(page.lookup(".prototype-hint"));
        }
    }

    @Test
    void loadDashboard_actionButtons_matchHeightsAndReviewCardStartsHidden() throws IOException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));
        service.addPlan(trip.id(), "Night market", LocalDate.of(2027, 1, 5), LocalTime.of(23, 30));
        VBox page = loadView("DashboardView.fxml", service);
        page.resize(722, 600);
        Scene scene = new Scene(page, 722, 600);
        scene.getRoot().applyCss();
        page.layout();
        page.layout();

        Button editButton = assertInstanceOf(Button.class, page.lookup("#editPlanButton"));
        Button reviewButton = assertInstanceOf(Button.class, page.lookup("#reviewPlanButton"));
        Button deleteButton = assertInstanceOf(Button.class, page.lookup("#deletePlanButton"));
        VBox reviewCard = assertInstanceOf(VBox.class, page.lookup("#detailReviewCard"));
        Label detailReviewLabel = getReviewLabel(getReviewScrollPane(page));
        assertEquals(editButton.getHeight(), deleteButton.getHeight(), 0.01);
        assertEquals(editButton.getHeight(), reviewButton.getHeight(), 0.01);
        assertEquals(Region.USE_PREF_SIZE, detailReviewLabel.getMinHeight());
        assertFalse(reviewCard.isVisible());
        assertFalse(reviewCard.isManaged());
    }

    @Test
    void loadDashboard_withoutPlans_displaysNoPlansDetailMessage() throws IOException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        VBox page = loadView("DashboardView.fxml", service);

        Label panelSubtitle = assertInstanceOf(Label.class,
                page.lookup(".dashboard-list-panel .panel-subtitle"));
        Label detailEmptyState = assertInstanceOf(Label.class, page.lookup("#detailEmptyState"));

        assertEquals("Select a plan to see its details.", panelSubtitle.getText());
        assertEquals("There are no plans to view.", detailEmptyState.getText());
        assertTrue(detailEmptyState.isVisible());
        assertTrue(detailEmptyState.isManaged());
    }

    @Test
    void loadDashboard_reviewedPlan_showsReviewActionAndCard() throws IOException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));
        Plan plan = service.addPlan(trip.id(), "Night market", LocalDate.of(2027, 1, 5), LocalTime.of(23, 30));
        service.setPlanReview(trip.id(), plan.id(), new Review(OptionalInt.of(4), Optional.of("Memorable.")));

        VBox page = loadView("DashboardView.fxml", service);
        Button reviewButton = assertInstanceOf(Button.class, page.lookup("#reviewPlanButton"));
        VBox reviewCard = assertInstanceOf(VBox.class, page.lookup("#detailReviewCard"));
        Label reviewLabel = getReviewLabel(getReviewScrollPane(page));

        assertEquals("Edit Review", reviewButton.getText());
        assertTrue(reviewCard.isVisible());
        assertTrue(reviewCard.isManaged());
        assertEquals("Rating: 4/5\nNotes: Memorable.", reviewLabel.getText());
    }

    @Test
    void tripDetail_actionsAndPlanHeader_haveRequestedOrderInOrganiseAndGallery() throws IOException {
        for (String resourceName : List.of("OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            LocalDate tripDate = resourceName.equals("GalleryView.fxml")
                    ? LocalDate.of(2027, 1, 1)
                    : LocalDate.of(2027, 1, 5);
            service.createTrip("Japan", tripDate, tripDate);
            VBox page = loadView(resourceName, service);
            Button editButton = assertInstanceOf(Button.class, page.lookup("#editTripButton"));
            Button reviewButton = assertInstanceOf(Button.class, page.lookup("#reviewTripButton"));
            Button addPlanButton = assertInstanceOf(Button.class, page.lookup("#addPlanButton"));
            Button deleteTripButton = assertInstanceOf(Button.class, page.lookup("#deleteTripButton"));
            VBox detailReviewCard = assertInstanceOf(VBox.class, page.lookup("#detailReviewCard"));
            ScrollPane reviewScrollPane = getReviewScrollPane(page);
            Label detailReviewLabel = getReviewLabel(reviewScrollPane);
            Label reviewHeading = assertInstanceOf(Label.class, page.lookup(".review-heading"));
            Label plansLabel = assertInstanceOf(Label.class, page.lookup("#plansLabel"));
            HBox actionBar = assertInstanceOf(HBox.class, editButton.getParent());
            HBox plansHeader = assertInstanceOf(HBox.class, plansLabel.getParent());

            assertSame(actionBar, reviewButton.getParent());
            assertSame(actionBar, deleteTripButton.getParent());
            assertEquals(editButton, actionBar.getChildren().get(0));
            assertEquals(reviewButton, actionBar.getChildren().get(1));
            assertEquals(deleteTripButton, actionBar.getChildren().get(2));
            assertEquals("Add Review", reviewButton.getText());
            assertEquals("Trip Review", reviewHeading.getText());
            assertSame(plansHeader, addPlanButton.getParent());
            assertEquals(plansLabel, plansHeader.getChildren().get(0));
            assertEquals(addPlanButton, plansHeader.getChildren().get(2));
            assertEquals("Plans (0)", plansLabel.getText());
            assertFalse(detailReviewCard.isVisible());
            assertFalse(detailReviewCard.isManaged());
            assertEquals("", detailReviewLabel.getText());
            assertTrue(deleteTripButton.getStyleClass().contains("danger-button"));
            assertFalse(reviewButton.isDisabled());
            assertFalse(deleteTripButton.isDisabled());
        }
    }

    @Test
    void tripDetail_actionButtons_haveEqualHeightsInOrganiseAndGallery() throws IOException {
        for (String resourceName : List.of("OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            LocalDate tripDate = resourceName.equals("GalleryView.fxml")
                    ? LocalDate.of(2027, 1, 1)
                    : LocalDate.of(2027, 1, 5);
            service.createTrip("Japan", tripDate, tripDate);

            VBox page = loadView(resourceName, service);
            page.resize(802, 646);
            Scene scene = new Scene(page, 802, 646);
            scene.getStylesheets().add(getClass().getResource("/doggo/ui/javafx/doggo.css").toExternalForm());
            scene.getRoot().applyCss();
            page.layout();
            page.layout();

            Button editTripButton = assertInstanceOf(Button.class, page.lookup("#editTripButton"));
            Button reviewTripButton = assertInstanceOf(Button.class, page.lookup("#reviewTripButton"));
            Button deleteTripButton = assertInstanceOf(Button.class, page.lookup("#deleteTripButton"));

            assertEquals(editTripButton.getHeight(), reviewTripButton.getHeight(), 0.01);
            assertEquals(editTripButton.getHeight(), deleteTripButton.getHeight(), 0.01);
            assertTrue(reviewTripButton.getStyleClass().contains("review-action-button"));
            assertEquals("0xf7f2e8ff", getBackgroundColor(reviewTripButton));
            assertEquals("0xa88f72ff", getBorderColor(reviewTripButton));
        }
    }

    @Test
    void tripDetail_existingReview_usesEditActionAndDisplaysReviewInBothViews() throws IOException {
        Review review = new Review(OptionalInt.of(4), Optional.of("A memorable journey."));
        for (String resourceName : List.of("OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            LocalDate tripDate = resourceName.equals("GalleryView.fxml")
                    ? LocalDate.of(2027, 1, 1)
                    : LocalDate.of(2027, 1, 5);
            Trip trip = service.createTrip("Japan", tripDate, tripDate);
            service.setTripReview(trip.id(), review);

            VBox page = loadView(resourceName, service);

            Button reviewButton = assertInstanceOf(Button.class, page.lookup("#reviewTripButton"));
            VBox detailReviewCard = assertInstanceOf(VBox.class, page.lookup("#detailReviewCard"));
            Label detailReviewLabel = getReviewLabel(getReviewScrollPane(page));
            assertEquals("Edit Review", reviewButton.getText());
            assertTrue(detailReviewCard.isVisible());
            assertTrue(detailReviewCard.isManaged());
            assertEquals("Rating: 4/5\nNotes: A memorable journey.", detailReviewLabel.getText());
        }
    }

    @Test
    void tripDetail_clearedReview_hidesCardAndRestoresAddActionInOrganiseAndGallery()
            throws IOException {
        for (String resourceName : List.of("OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            LocalDate tripDate = resourceName.equals("GalleryView.fxml")
                    ? LocalDate.of(2027, 1, 1)
                    : LocalDate.of(2027, 1, 5);
            Trip trip = service.createTrip("Japan", tripDate, tripDate);
            service.setTripReview(trip.id(), new Review(OptionalInt.of(4), Optional.of("A memorable journey.")));

            FXMLLoader loader = createViewLoader(resourceName, service);
            VBox page = assertInstanceOf(VBox.class, loader.load());
            Object controller = loader.getController();
            VBox detailReviewCard = assertInstanceOf(VBox.class, page.lookup("#detailReviewCard"));
            Button reviewButton = assertInstanceOf(Button.class, page.lookup("#reviewTripButton"));
            Label detailReviewLabel = getReviewLabel(getReviewScrollPane(page));
            assertTrue(detailReviewCard.isVisible());
            assertEquals("Edit Review", reviewButton.getText());

            service.removeTripReview(trip.id());
            if (controller instanceof OrganiseController organiseController) {
                organiseController.refreshAndSelect(trip.id());
            } else {
                assertInstanceOf(GalleryController.class, controller).refreshAndSelect(trip.id());
            }

            assertFalse(detailReviewCard.isVisible());
            assertFalse(detailReviewCard.isManaged());
            assertEquals("Add Review", reviewButton.getText());
            assertEquals("", detailReviewLabel.getText());
        }
    }

    @Test
    void tripDetail_reviewLabel_preservesPreferredHeightInOrganiseAndGallery() throws IOException {
        for (String resourceName : List.of("OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            LocalDate tripDate = resourceName.equals("GalleryView.fxml")
                    ? LocalDate.of(2027, 1, 1)
                    : LocalDate.of(2027, 1, 5);
            service.createTrip("Japan", tripDate, tripDate);
            VBox page = loadView(resourceName, service);

            Label detailReviewLabel = getReviewLabel(getReviewScrollPane(page));

            assertEquals(Region.USE_PREF_SIZE, detailReviewLabel.getMinHeight());
        }
    }

    @Test
    void tripDetail_longReview_preservesPlanViewportInOrganiseAndGallery() throws IOException {
        String longNotes = "A memorable day with many details. ".repeat(80);
        String longTitle = "W".repeat(Trip.MAX_TITLE_LENGTH);
        for (String resourceName : List.of("OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            LocalDate tripDate = resourceName.equals("GalleryView.fxml")
                    ? LocalDate.of(2027, 1, 1)
                    : LocalDate.of(2027, 1, 5);
            Trip trip = service.createTrip(longTitle, tripDate, tripDate);
            service.addPlan(trip.id(), "Breakfast", tripDate, LocalTime.of(9, 0));
            service.addPlan(trip.id(), "Museum", tripDate, LocalTime.of(11, 0));
            service.setTripReview(trip.id(), new Review(OptionalInt.of(3), Optional.of(longNotes)));

            VBox page = loadView(resourceName, service);
            page.resize(802, 646);
            Scene scene = new Scene(page, 802, 646);
            scene.getRoot().applyCss();
            page.layout();
            page.layout();

            ScrollPane reviewScrollPane = getReviewScrollPane(page);
            ScrollBar verticalScrollBar = findScrollBar(reviewScrollPane, Orientation.VERTICAL);
            ListView<?> planList = assertInstanceOf(ListView.class, page.lookup("#planList"));
            GridPane panels = findMainLayout(page);
            VBox detailPanel = assertInstanceOf(VBox.class, panels.getChildren().getLast());
            VBox reviewCard = assertInstanceOf(VBox.class, reviewScrollPane.getParent());

            assertTrue(verticalScrollBar.isVisible());
            assertFalse(findScrollBar(reviewScrollPane, Orientation.HORIZONTAL).isVisible());
            assertEquals(ReviewCardSupport.MAX_CARD_HEIGHT, reviewCard.getMaxHeight(), 0.01);
            assertEquals(ReviewCardSupport.MAX_CARD_HEIGHT, reviewCard.getMinHeight(), 0.01);
            assertEquals(reviewCard.getMinHeight(), reviewCard.getPrefHeight(), 0.01);
            assertEquals(reviewScrollPane.getMinHeight(), reviewScrollPane.getPrefHeight(), 0.01);
            assertTrue(reviewCard.getHeight() <= reviewCard.getMaxHeight() + 0.01);
            assertTrue(reviewCard.getHeight() >= reviewCard.getMinHeight() - 0.01);
            assertTrue(reviewScrollPane.getViewportBounds().getHeight() > 0);
            assertTrue(planList.getHeight() >= planList.getMinHeight() - 0.01);
            assertTrue(planList.getHeight() >= 90.0);
            assertTrue(planList.getBoundsInParent().getMaxY() <= detailPanel.getHeight() + 0.01);
            assertInstanceOf(ListCell.class, planList.lookup(".list-cell"));
        }
    }

    @Test
    void tripDetail_combinedReview_showsRatingAndNotesWithoutClippingInOrganiseAndGallery()
            throws IOException {
        Review review = new Review(OptionalInt.of(3), Optional.of("Cool trip"));
        for (String resourceName : List.of("OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            LocalDate tripDate = resourceName.equals("GalleryView.fxml")
                    ? LocalDate.of(2027, 1, 1)
                    : LocalDate.of(2027, 1, 5);
            Trip trip = service.createTrip("Japan", tripDate, tripDate);
            service.setTripReview(trip.id(), review);

            VBox page = loadView(resourceName, service);
            page.resize(802, 646);
            Scene scene = new Scene(page, 802, 646);
            scene.getRoot().applyCss();
            page.layout();
            page.layout();

            ScrollPane reviewScrollPane = getReviewScrollPane(page);
            Label reviewLabel = getReviewLabel(reviewScrollPane);
            VBox reviewCard = assertInstanceOf(VBox.class, reviewScrollPane.getParent());

            assertEquals("Rating: 3/5\nNotes: Cool trip", reviewLabel.getText());
            assertFalse(findScrollBar(reviewScrollPane, Orientation.VERTICAL).isVisible());
            assertTrue(reviewLabel.getHeight() <= reviewScrollPane.getViewportBounds().getHeight() + 0.01);
            assertEquals(reviewCard.getMinHeight(), reviewCard.getHeight(), 0.01);
            assertEquals(reviewCard.getPrefHeight(), reviewCard.getHeight(), 0.01);
            assertTrue(reviewCard.getHeight() < 120.0);
        }
    }

    @Test
    void tripDetail_planHeading_showsSelectedTripPlanCount() throws IOException {
        for (String resourceName : List.of("OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            LocalDate tripDate = resourceName.equals("GalleryView.fxml")
                    ? LocalDate.of(2027, 1, 1)
                    : LocalDate.of(2027, 1, 5);
            Trip trip = service.createTrip("Japan", tripDate, tripDate);
            service.addPlan(trip.id(), "Breakfast", tripDate, LocalTime.of(9, 0));
            service.addPlan(trip.id(), "Museum", tripDate, LocalTime.of(11, 0));
            service.addPlan(trip.id(), "Dinner", tripDate, LocalTime.of(19, 0));

            VBox page = loadView(resourceName, service);

            Label plansLabel = assertInstanceOf(Label.class, page.lookup("#plansLabel"));
            assertEquals("Plans (3)", plansLabel.getText());
        }
    }

    @Test
    void appShell_minimumSize_preservesLayoutAcrossNavigationAndSelection() throws IOException {
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

    @Test
    void setOpeningSizeAsMinimum_openStage_preventsShrinkingBelowOpeningSize()
            throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<DialogSize> stageTask = new FutureTask<>(() -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(new VBox(), 1180, 760));
            stage.show();
            DoggoApplication.setOpeningSizeAsMinimum(stage);
            DialogSize size = new DialogSize(stage.getWidth(), stage.getHeight(), stage.getMinWidth(),
                    stage.getMinHeight());
            stage.close();
            return size;
        });
        Platform.runLater(stageTask);

        DialogSize size = stageTask.get(5, TimeUnit.SECONDS);
        assertEquals(size.width(), size.minWidth(), 0.01);
        assertEquals(size.height(), size.minHeight(), 0.01);
    }

    @Test
    void updatePlanCell_planWithYear_rendersFullDateAndCompactActions() {
        Plan plan = new Plan(UUID.randomUUID(), "Night market", LocalDate.of(2026, 8, 30),
                LocalTime.of(23, 30));
        PlanCell cell = new PlanCell();

        cell.updateItem(plan, false);

        HBox card = assertInstanceOf(HBox.class, cell.getGraphic());
        VBox details = assertInstanceOf(VBox.class, card.getChildren().getFirst());
        HBox dateAndTime = assertInstanceOf(HBox.class, details.getChildren().getFirst());
        Label dateLabel = assertInstanceOf(Label.class, dateAndTime.getChildren().getFirst());
        Label timeLabel = assertInstanceOf(Label.class, dateAndTime.getChildren().get(1));
        Label reviewCue = assertInstanceOf(Label.class, dateAndTime.getChildren().getLast());
        Label destinationLabel = assertInstanceOf(Label.class, details.getChildren().getLast());
        assertEquals("30 Aug 2026 (Sun)", dateLabel.getText());
        assertEquals("23:30", timeLabel.getText());
        assertEquals(3, dateAndTime.getChildren().size());
        assertFalse(reviewCue.isVisible());
        assertFalse(reviewCue.isManaged());
        assertEquals(plan.destination(), destinationLabel.getTooltip().getText());
        assertFalse(destinationLabel.isWrapText());
        assertEquals(OverrunStyle.ELLIPSIS, destinationLabel.getTextOverrun());
        HBox actions = assertInstanceOf(HBox.class, card.getChildren().getLast());
        assertEquals("Details", assertInstanceOf(Button.class, actions.getChildren().getFirst()).getText());
        assertEquals(1, actions.getChildren().size());
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
    void planCell_detailsButton_invokesHandlerWithRenderedPlan() {
        Plan plan = new Plan(UUID.randomUUID(), "Night market", LocalDate.of(2026, 8, 30),
                LocalTime.of(23, 30));
        AtomicReference<Plan> editedPlan = new AtomicReference<>();
        PlanCell cell = new PlanCell(editedPlan::set);

        cell.updateItem(plan, false);

        Button detailsButton = assertInstanceOf(Button.class,
                assertInstanceOf(HBox.class,
                        assertInstanceOf(HBox.class, cell.getGraphic()).getChildren().getLast())
                        .getChildren().getFirst());
        detailsButton.fire();

        assertSame(plan, editedPlan.get());
    }

    @Test
    void planCell_reviewCue_displaysRatingWithoutExpandingCell() {
        Plan plan = new Plan(UUID.randomUUID(), "Night market", LocalDate.of(2026, 8, 30),
                LocalTime.of(23, 30));
        Plan reviewedPlan = plan.withReview(new Review(OptionalInt.of(4), Optional.empty()));
        PlanCell cell = new PlanCell();

        cell.updateItem(reviewedPlan, false);

        HBox card = assertInstanceOf(HBox.class, cell.getGraphic());
        HBox dateAndTime = assertInstanceOf(HBox.class,
                assertInstanceOf(VBox.class, card.getChildren().getFirst()).getChildren().getFirst());
        Label reviewCue = assertInstanceOf(Label.class, dateAndTime.getChildren().getLast());
        assertEquals("4/5 stars", reviewCue.getText());
        assertTrue(reviewCue.isVisible());
        assertTrue(reviewCue.isManaged());
        assertEquals(Region.USE_PREF_SIZE, reviewCue.getMinWidth());
        assertEquals(Region.USE_PREF_SIZE, reviewCue.getMaxWidth());
        assertEquals("Details", assertInstanceOf(Button.class,
                assertInstanceOf(HBox.class, card.getChildren().getLast()).getChildren().getFirst()).getText());
    }

    @Test
    void planCell_reviewCue_displaysReviewedForNotesOnly() {
        Plan plan = new Plan(UUID.randomUUID(), "Night market", LocalDate.of(2026, 8, 30),
                LocalTime.of(23, 30));
        Plan reviewedPlan = plan.withReview(new Review(OptionalInt.empty(), Optional.of("Memorable.")));
        PlanCell cell = new PlanCell();

        cell.updateItem(reviewedPlan, false);

        HBox card = assertInstanceOf(HBox.class, cell.getGraphic());
        HBox dateAndTime = assertInstanceOf(HBox.class,
                assertInstanceOf(VBox.class, card.getChildren().getFirst()).getChildren().getFirst());
        Label reviewCue = assertInstanceOf(Label.class, dateAndTime.getChildren().getLast());
        assertEquals("Reviewed", reviewCue.getText());
        assertFalse(reviewCue.isTextTruncated());
    }

    @Test
    void loadTripViews_longPlanNames_preserveReviewCueAndDetailsButton() throws IOException {
        String destination = "W".repeat(Plan.MAX_DESTINATION_LENGTH);
        for (String viewName : List.of("OrganiseView.fxml", "GalleryView.fxml")) {
            DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
            LocalDate tripDate = viewName.startsWith("Organise")
                    ? LocalDate.of(2027, 1, 5)
                    : LocalDate.of(2027, 1, 1);
            Trip trip = service.createTrip("Japan", tripDate, tripDate);
            Plan plan = service.addPlan(trip.id(), destination, tripDate, LocalTime.of(9, 0));
            service.setPlanReview(trip.id(), plan.id(), new Review(OptionalInt.of(4), Optional.empty()));

            VBox page = loadView(viewName, service);
            page.resize(722, 600);
            Scene scene = new Scene(page, 722, 600);
            scene.getRoot().applyCss();
            page.layout();
            page.layout();

            ListView<?> planList = assertInstanceOf(ListView.class, page.lookup("#planList"));
            ListCell<?> cell = assertInstanceOf(ListCell.class, planList.lookup(".list-cell"));
            HBox card = assertInstanceOf(HBox.class, cell.getGraphic());
            VBox details = assertInstanceOf(VBox.class, card.getChildren().getFirst());
            HBox dateAndTime = assertInstanceOf(HBox.class, details.getChildren().getFirst());
            Label destinationLabel = assertInstanceOf(Label.class, details.getChildren().getLast());
            Label reviewCue = assertInstanceOf(Label.class, dateAndTime.getChildren().getLast());
            Button detailsButton = assertInstanceOf(Button.class,
                    assertInstanceOf(HBox.class, card.getChildren().getLast()).getChildren().getFirst());

            assertTrue(destinationLabel.isTextTruncated());
            assertEquals("4/5 stars", reviewCue.getText());
            assertFalse(reviewCue.isTextTruncated());
            assertFalse(detailsButton.isTextTruncated());
            assertHorizontalScrollbarHidden(planList);
        }
    }

    @Test
    void planDetailsDialog_reviewedPlan_displaysManagementActionsAndReview()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));
        Plan plan = service.addPlan(trip.id(), "Night market", LocalDate.of(2027, 1, 5), LocalTime.of(23, 30));
        Plan reviewedPlan = service.setPlanReview(trip.id(), plan.id(),
                new Review(OptionalInt.of(4), Optional.of("Memorable.")));
        FutureTask<PlanDetailsState> dialogTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            PlanDetailsDialog dialog = new PlanDetailsDialog(service, trip, reviewedPlan, owner);
            VBox content = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            Button editButton = assertInstanceOf(Button.class, content.lookup("#planEditButton"));
            Button reviewButton = assertInstanceOf(Button.class, content.lookup("#planReviewButton"));
            Button deleteButton = assertInstanceOf(Button.class, content.lookup("#planDeleteButton"));
            VBox reviewCard = assertInstanceOf(VBox.class, content.lookup(".review-card"));
            ScrollPane reviewScrollPane = assertInstanceOf(ScrollPane.class,
                    content.lookup(".review-scroll-pane"));
            Label reviewLabel = assertInstanceOf(Label.class, reviewScrollPane.getContent());
            return new PlanDetailsState(dialog.getTitle(), editButton.getText(), reviewButton.getText(),
                    deleteButton.getText(), reviewCard.isVisible(), reviewLabel.getText(), dialog.isResizable());
        });
        Platform.runLater(dialogTask);

        PlanDetailsState state = dialogTask.get(5, TimeUnit.SECONDS);
        assertEquals("Plan details", state.title());
        assertEquals("Edit plan", state.editText());
        assertEquals("Edit Review", state.reviewText());
        assertEquals("Delete plan", state.deleteText());
        assertTrue(state.reviewVisible());
        assertEquals("Rating: 4/5\nNotes: Memorable.", state.reviewTextContent());
        assertTrue(state.resizable());
    }

    @Test
    void planDetailsDialog_openingHeight_matchesVisibleContent()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));
        Plan plan = service.addPlan(trip.id(), "Night market", LocalDate.of(2027, 1, 5), LocalTime.of(23, 30));
        Plan reviewedPlan = service.setPlanReview(trip.id(), plan.id(),
                new Review(OptionalInt.of(4), Optional.of("Memorable.")));
        AtomicReference<PlanDetailsDialog> compactDialog = new AtomicReference<>();
        AtomicReference<PlanDetailsDialog> reviewedDialog = new AtomicReference<>();
        AtomicReference<Stage> ownerStage = new AtomicReference<>();
        FutureTask<Void> showTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            owner.show();
            ownerStage.set(owner);
            compactDialog.set(new PlanDetailsDialog(service, trip, plan, owner));
            reviewedDialog.set(new PlanDetailsDialog(service, trip, reviewedPlan, owner));
            compactDialog.get().show();
            reviewedDialog.get().show();
            return null;
        });
        Platform.runLater(showTask);
        showTask.get(5, TimeUnit.SECONDS);

        FutureTask<PlanDialogHeights> captureTask = new FutureTask<>(() -> {
            double compactHeight = getDialogStage(compactDialog.get()).getHeight();
            double reviewedHeight = getDialogStage(reviewedDialog.get()).getHeight();
            compactDialog.get().close();
            reviewedDialog.get().close();
            ownerStage.get().close();
            return new PlanDialogHeights(compactHeight, reviewedHeight);
        });
        Platform.runLater(captureTask);

        PlanDialogHeights heights = captureTask.get(5, TimeUnit.SECONDS);
        assertTrue(heights.compactHeight() < 520,
                () -> "Expected compact dialog below the old fixed height, got " + heights.compactHeight());
        assertTrue(heights.reviewedHeight() > heights.compactHeight(),
                () -> "Expected reviewed dialog to be taller: " + heights);
    }

    @Test
    void planDeletionDialog_displaysNamesAndSafeConfirmationActions()
            throws InterruptedException, ExecutionException, TimeoutException {
        Plan plan = new Plan(UUID.randomUUID(), "Night market", LocalDate.of(2026, 8, 30),
                LocalTime.of(23, 30));
        FutureTask<DeletionDialogState> dialogTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            DeletionConfirmationDialog dialog = DeletionConfirmationDialog.forPlan(plan, "Japan", owner);
            DialogPane dialogPane = dialog.getDialogPane();
            Label description = assertInstanceOf(Label.class, dialogPane.getContent());
            Button yesButton = assertInstanceOf(Button.class,
                    dialogPane.lookupButton(dialogPane.getButtonTypes().getFirst()));
            Button noButton = assertInstanceOf(Button.class,
                    dialogPane.lookupButton(dialogPane.getButtonTypes().getLast()));
            return new DeletionDialogState(dialog.getTitle(), description.getText(), yesButton.getText(),
                    yesButton.getStyleClass(), noButton.getText(), noButton.getStyleClass(),
                    noButton.isDefaultButton(), noButton.isCancelButton());
        });
        Platform.runLater(dialogTask);

        DeletionDialogState state = dialogTask.get(5, TimeUnit.SECONDS);
        assertEquals("Delete a plan", state.title());
        assertEquals("Do you really want to delete the plan \"Night market\" from the trip \"Japan\"?",
                state.description());
        assertEquals(List.of("Yes", "No"), state.buttonTexts());
        assertTrue(state.yesStyles().contains("danger-button"));
        assertTrue(state.noStyles().contains("delete-cancel-button"));
        assertEquals("No", state.noText());
        assertTrue(state.noIsDefault());
        assertTrue(state.noIsCancel());
    }

    @Test
    void planDeletionDialog_appliesDistinctRenderedButtonColors()
            throws InterruptedException, ExecutionException, TimeoutException {
        Plan plan = new Plan(UUID.randomUUID(), "Night market", LocalDate.of(2026, 8, 30),
                LocalTime.of(23, 30));
        FutureTask<DeletionButtonStyleState> styleTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            DeletionConfirmationDialog dialog = DeletionConfirmationDialog.forPlan(plan, "Japan", owner);
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getScene().getRoot().applyCss();
            dialogPane.getScene().getRoot().layout();
            Button yesButton = assertInstanceOf(Button.class,
                    dialogPane.lookupButton(dialogPane.getButtonTypes().getFirst()));
            Button noButton = assertInstanceOf(Button.class,
                    dialogPane.lookupButton(dialogPane.getButtonTypes().getLast()));
            return new DeletionButtonStyleState(getBackgroundColor(yesButton), getBackgroundColor(noButton));
        });
        Platform.runLater(styleTask);

        DeletionButtonStyleState state = styleTask.get(5, TimeUnit.SECONDS);
        assertEquals("0xe5a08eff", state.yesBackground());
        assertEquals("0xfffaf1ff", state.noBackground());
    }

    @Test
    void tripDeletionDialog_displaysNameAndSafeConfirmationActions()
            throws InterruptedException, ExecutionException, TimeoutException {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        FutureTask<DeletionDialogState> dialogTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            DeletionConfirmationDialog dialog = DeletionConfirmationDialog.forTrip(trip, owner);
            DialogPane dialogPane = dialog.getDialogPane();
            Label description = assertInstanceOf(Label.class, dialogPane.getContent());
            Button yesButton = assertInstanceOf(Button.class,
                    dialogPane.lookupButton(dialogPane.getButtonTypes().getFirst()));
            Button noButton = assertInstanceOf(Button.class,
                    dialogPane.lookupButton(dialogPane.getButtonTypes().getLast()));
            return new DeletionDialogState(dialog.getTitle(), description.getText(), yesButton.getText(),
                    yesButton.getStyleClass(), noButton.getText(), noButton.getStyleClass(),
                    noButton.isDefaultButton(), noButton.isCancelButton());
        });
        Platform.runLater(dialogTask);

        DeletionDialogState state = dialogTask.get(5, TimeUnit.SECONDS);
        assertEquals("Delete a trip", state.title());
        assertEquals("Do you really want to delete the trip \"Japan\"?", state.description());
        assertEquals(List.of("Yes", "No"), state.buttonTexts());
        assertTrue(state.yesStyles().contains("danger-button"));
        assertTrue(state.noStyles().contains("delete-cancel-button"));
        assertTrue(state.noIsDefault());
        assertTrue(state.noIsCancel());
    }

    private static String getBackgroundColor(Button button) {
        return button.getBackground().getFills().getFirst().getFill().toString();
    }

    private static String getBorderColor(Button button) {
        return button.getBorder().getStrokes().getFirst().getTopStroke().toString();
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
    void tripReviewDialog_emptyReview_hasOptionalNumericFieldsAndEnabledSave()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        FutureTask<ReviewDialogState> dialogTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            ReviewDialog<Trip> dialog = ReviewDialog.forTrip(service, trip, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            List<String> ratingTexts = List.of(
                    assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton1")).getText(),
                    assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton2")).getText(),
                    assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton3")).getText(),
                    assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton4")).getText(),
                    assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton5")).getText());
            TextArea notesArea = assertInstanceOf(TextArea.class, form.lookup("#reviewNotesField"));
            Button saveButton = assertInstanceOf(Button.class,
                    dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().getFirst()));
            boolean isSaveDisabled = saveButton.isDisabled();
            boolean isSaveDefault = saveButton.isDefaultButton();
            saveButton.fire();
            boolean isReviewPresent = service.getTrip(trip.id()).orElseThrow().review().isPresent();
            return new ReviewDialogState(dialog.getTitle(), ratingTexts, notesArea.getText(),
                    isSaveDisabled, isSaveDefault, dialog.isResizable(), isReviewPresent);
        });
        Platform.runLater(dialogTask);

        ReviewDialogState state = dialogTask.get(5, TimeUnit.SECONDS);
        assertEquals("Review a trip", state.title());
        assertEquals(List.of("1", "2", "3", "4", "5"), state.ratingTexts());
        assertEquals("", state.notes());
        assertFalse(state.saveDisabled());
        assertTrue(state.saveDefault());
        assertTrue(state.resizable());
        assertFalse(state.isReviewPresentAfterSave());
    }

    @Test
    void tripReviewDialog_submitRatingAndNotes_persistsCombinedReview()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        FutureTask<Review> saveTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            ReviewDialog<Trip> dialog = ReviewDialog.forTrip(service, trip, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton2")).fire();
            assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton4")).fire();
            assertInstanceOf(TextArea.class, form.lookup("#reviewNotesField"))
                    .setText("  A memorable journey.  ");
            assertInstanceOf(Button.class,
                    dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().getFirst())).fire();
            return service.getTrip(trip.id()).orElseThrow().review().orElseThrow();
        });
        Platform.runLater(saveTask);

        Review savedReview = saveTask.get(5, TimeUnit.SECONDS);
        assertEquals(OptionalInt.of(4), savedReview.rating());
        assertEquals(Optional.of("A memorable journey."), savedReview.text());
    }

    @Test
    void tripReviewDialog_existingReview_prefillsAndAllowsRemoval()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        Trip reviewedTrip = service.setTripReview(trip.id(),
                new Review(OptionalInt.of(4), Optional.of("A memorable journey.")));
        FutureTask<ReviewRemovalState> removalTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            ReviewDialog<Trip> dialog = ReviewDialog.forTrip(service, reviewedTrip, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            ToggleButton ratingButton = assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton4"));
            TextArea notesArea = assertInstanceOf(TextArea.class, form.lookup("#reviewNotesField"));
            boolean wasRatingSelected = ratingButton.isSelected();
            String existingNotes = notesArea.getText();
            ratingButton.fire();
            notesArea.clear();
            assertInstanceOf(Button.class,
                    dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().getFirst())).fire();
            boolean isReviewPresent = service.getTrip(trip.id()).orElseThrow().review().isPresent();
            return new ReviewRemovalState(wasRatingSelected, existingNotes,
                    ratingButton.isSelected(), isReviewPresent);
        });
        Platform.runLater(removalTask);

        ReviewRemovalState state = removalTask.get(5, TimeUnit.SECONDS);
        assertTrue(state.wasRatingSelected());
        assertEquals("A memorable journey.", state.existingNotes());
        assertFalse(state.isRatingSelectedAfterClick());
        assertFalse(state.isReviewPresentAfterSave());
    }

    @Test
    void planReviewDialog_submitRatingAndNotes_persistsCombinedReview()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Senso-ji Temple", LocalDate.of(2027, 1, 6),
                LocalTime.of(9, 30));
        FutureTask<Review> saveTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            ReviewDialog<Plan> dialog = ReviewDialog.forPlan(service, trip, plan, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton3")).fire();
            assertInstanceOf(TextArea.class, form.lookup("#reviewNotesField"))
                    .setText("  A memorable stop.  ");
            assertInstanceOf(Button.class,
                    dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().getFirst())).fire();
            return service.getTrip(trip.id()).orElseThrow().plans().getFirst().review().orElseThrow();
        });
        Platform.runLater(saveTask);

        Review savedReview = saveTask.get(5, TimeUnit.SECONDS);
        assertEquals(OptionalInt.of(3), savedReview.rating());
        assertEquals(Optional.of("A memorable stop."), savedReview.text());
    }

    @Test
    void planReviewDialog_existingReview_prefillsAndAllowsRemoval()
            throws InterruptedException, ExecutionException, TimeoutException {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        Trip trip = service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 9));
        Plan plan = service.addPlan(trip.id(), "Senso-ji Temple", LocalDate.of(2027, 1, 6),
                LocalTime.of(9, 30));
        Plan reviewedPlan = service.setPlanReview(trip.id(), plan.id(),
                new Review(OptionalInt.of(4), Optional.of("A memorable stop.")));
        FutureTask<ReviewRemovalState> removalTask = new FutureTask<>(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new VBox()));
            ReviewDialog<Plan> dialog = ReviewDialog.forPlan(service, trip, reviewedPlan, owner);
            VBox form = assertInstanceOf(VBox.class, dialog.getDialogPane().getContent());
            ToggleButton ratingButton = assertInstanceOf(ToggleButton.class, form.lookup("#ratingButton4"));
            TextArea notesArea = assertInstanceOf(TextArea.class, form.lookup("#reviewNotesField"));
            boolean wasRatingSelected = ratingButton.isSelected();
            String existingNotes = notesArea.getText();
            ratingButton.fire();
            notesArea.clear();
            assertInstanceOf(Button.class,
                    dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().getFirst())).fire();
            boolean isReviewPresent = service.getTrip(trip.id()).orElseThrow().plans().getFirst().review().isPresent();
            return new ReviewRemovalState(wasRatingSelected, existingNotes,
                    ratingButton.isSelected(), isReviewPresent);
        });
        Platform.runLater(removalTask);

        ReviewRemovalState state = removalTask.get(5, TimeUnit.SECONDS);
        assertTrue(state.wasRatingSelected());
        assertEquals("A memorable stop.", state.existingNotes());
        assertFalse(state.isRatingSelectedAfterClick());
        assertFalse(state.isReviewPresentAfterSave());
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

    private record DeletionDialogState(String title, String description, String yesText, List<String> yesStyles,
                                       String noText, List<String> noStyles, boolean noIsDefault, boolean noIsCancel) {
        private List<String> buttonTexts() {
            return List.of(yesText, noText);
        }
    }

    private record DeletionButtonStyleState(String yesBackground, String noBackground) {
    }

    private record DialogSize(double width, double height, double minWidth, double minHeight) {
    }

    private record ReviewDialogState(String title, List<String> ratingTexts, String notes,
                                     boolean saveDisabled, boolean saveDefault, boolean resizable,
                                     boolean isReviewPresentAfterSave) {
    }

    private record ReviewRemovalState(boolean wasRatingSelected, String existingNotes,
                                      boolean isRatingSelectedAfterClick, boolean isReviewPresentAfterSave) {
    }

    private record PlanDetailsState(String title, String editText, String reviewText, String deleteText,
                                    boolean reviewVisible, String reviewTextContent, boolean resizable) {
    }

    private record PlanDialogHeights(double compactHeight, double reviewedHeight) {
    }

    private static Stage getDialogStage(PlanDetailsDialog dialog) {
        return (Stage) dialog.getDialogPane().getScene().getWindow();
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

    private static ScrollPane getReviewScrollPane(VBox page) {
        return assertInstanceOf(ScrollPane.class, page.lookup("#detailReviewScrollPane"));
    }

    private static Label getReviewLabel(ScrollPane reviewScrollPane) {
        return assertInstanceOf(Label.class, reviewScrollPane.getContent());
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
                    ? "From " + expectedText
                    : expectedText;
            assertEquals(expectedLabelText, displayedText.toString());
            if (labelId.equals("#detailTripLabel")) {
                assertTrue(label.getChildren().subList(1, label.getChildren().size()).stream()
                        .allMatch(child -> assertInstanceOf(Text.class, child)
                                .getStyleClass().contains("detail-trip-name-text")));
            }
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
        HBox actions = assertInstanceOf(HBox.class, card.getChildren().getLast());
        Button detailsButton = assertInstanceOf(Button.class, actions.getChildren().getFirst());
        assertEquals("Details", detailsButton.getText());
        assertFalse(detailsButton.isTextTruncated());
        assertEquals(actions.prefWidth(-1), actions.getWidth(), 0.01);
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

    private static ScrollBar findScrollBar(ScrollPane scrollPane, Orientation orientation) {
        return scrollPane.lookupAll(".scroll-bar").stream()
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
