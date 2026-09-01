package doggo.ui.javafx;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Trip;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Displays a modal form for creating or editing a Trip.
 */
final class TripCreationDialog extends Dialog<Trip> {
    private final DoggoService service;
    private final Optional<Trip> tripBeingEdited;
    private final LocalDate earliestPlanDate;
    private final LocalDate latestPlanDate;
    private final ButtonType submitButtonType;
    private final TextField titleField = new TextField();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final Label validationLabel = new Label();
    private Trip createdTrip;

    /**
     * Creates a modal Trip form owned by the specified window.
     *
     * @param service Application service used to create the Trip.
     * @param owner Window that owns this dialog.
     */
    TripCreationDialog(DoggoService service, Window owner) {
        this(service, Optional.empty(), owner);
    }

    /**
     * Creates a modal Trip form for editing the specified Trip.
     *
     * @param service Application service used to update the Trip.
     * @param trip Trip being edited.
     * @param owner Window that owns this dialog.
     */
    TripCreationDialog(DoggoService service, Trip trip, Window owner) {
        this(service, Optional.of(Objects.requireNonNull(trip)), owner);
    }

    /**
     * Creates a modal Trip form in the specified mode.
     *
     * @param service Application service used to persist the Trip.
     * @param tripBeingEdited Trip being edited, or empty for creation.
     * @param owner Window that owns this dialog.
     */
    private TripCreationDialog(DoggoService service, Optional<Trip> tripBeingEdited, Window owner) {
        this.service = Objects.requireNonNull(service);
        this.tripBeingEdited = Objects.requireNonNull(tripBeingEdited);
        List<Plan> plans = tripBeingEdited.map(Trip::plans).orElse(List.of());
        earliestPlanDate = plans.stream().map(Plan::date).min(LocalDate::compareTo).orElse(null);
        latestPlanDate = plans.stream().map(Plan::date).max(LocalDate::compareTo).orElse(null);
        submitButtonType = new ButtonType(tripBeingEdited.isPresent() ? "Save changes" : "Create trip",
                ButtonBar.ButtonData.OK_DONE);
        initOwner(Objects.requireNonNull(owner));
        initModality(javafx.stage.Modality.WINDOW_MODAL);
        setTitle(tripBeingEdited.isPresent() ? "Edit trip" : "Create a trip");

        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        dialogPane.setContent(createForm());
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(TripCreationDialog.class.getResource("doggo.css")).toExternalForm());

        Button submitButton = (Button) dialogPane.lookupButton(submitButtonType);
        submitButton.setDefaultButton(true);
        submitButton.addEventFilter(ActionEvent.ACTION, this::handleSubmit);
        setResultConverter(buttonType -> buttonType == submitButtonType ? createdTrip : null);
        DialogWindowSupport.enableExpandOnly(this, titleField);
        updateValidation(submitButton);
    }

    /**
     * Creates the Trip form content with default dates from the service Clock.
     *
     * @return Form content.
     */
    private VBox createForm() {
        LocalDate currentDate = service.getCurrentDate();
        titleField.setPromptText("e.g. A week in Japan");
        if (tripBeingEdited.isPresent()) {
            Trip trip = tripBeingEdited.get();
            titleField.setText(trip.title());
            startDatePicker.setValue(trip.startDate());
            endDatePicker.setValue(trip.endDate());
        } else {
            startDatePicker.setValue(currentDate);
            endDatePicker.setValue(currentDate);
        }
        startDatePicker.setDayCellFactory(picker -> createDateCell(earliestPlanDate, true));
        endDatePicker.setDayCellFactory(picker -> createDateCell(latestPlanDate, false));
        titleField.getStyleClass().add("form-field");
        startDatePicker.getStyleClass().add("form-field");
        endDatePicker.getStyleClass().add("form-field");

        GridPane fields = new GridPane();
        fields.setHgap(14);
        fields.setVgap(12);
        fields.setPadding(new Insets(6, 0, 0, 0));
        ColumnConstraints labelColumn = new ColumnConstraints();
        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);
        fields.getColumnConstraints().addAll(labelColumn, inputColumn);
        addField(fields, 0, "Trip name", titleField);
        addField(fields, 1, "Starts", startDatePicker);
        addField(fields, 2, "Ends", endDatePicker);

        validationLabel.getStyleClass().add("form-error");
        validationLabel.setWrapText(true);
        validationLabel.setManaged(true);
        validationLabel.setVisible(false);

        titleField.textProperty().addListener((observable, oldValue, newValue) -> updateValidation());
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateValidation());
        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateValidation());

        String introductionText = tripBeingEdited.isPresent()
                ? "Update your journey's name and date range."
                : "Give your next journey a name and a date range to get started.";
        Label introduction = new Label(introductionText);
        introduction.getStyleClass().add("form-intro");
        VBox form = new VBox(10, introduction, fields, validationLabel);
        form.getStyleClass().add("trip-form");
        form.setPadding(new Insets(8, 10, 4, 10));
        return form;
    }

    /**
     * Creates a date cell that prevents an edited Trip from excluding Plans.
     *
     * @param boundaryDate Plan boundary that the selected date must include.
     * @param isStartDate Whether the cell belongs to the start-date picker.
     * @return Configured date cell.
     */
    private static DateCell createDateCell(LocalDate boundaryDate, boolean isStartDate) {
        return new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean isEmpty) {
                super.updateItem(date, isEmpty);
                boolean excludesPlan = boundaryDate != null && (isStartDate
                        ? date != null && date.isAfter(boundaryDate)
                        : date != null && date.isBefore(boundaryDate));
                setDisable(isEmpty || excludesPlan);
            }
        };
    }

    /**
     * Adds one labelled field to the Trip form grid.
     *
     * @param fields Grid receiving the field.
     * @param row Grid row for the field.
     * @param labelText Field label text.
     * @param field Field control.
     */
    private static void addField(GridPane fields, int row, String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        fields.add(label, 0, row);
        fields.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
        if (field instanceof TextField textField) {
            textField.setMaxWidth(Double.MAX_VALUE);
        }
        if (field instanceof DatePicker datePicker) {
            datePicker.setMaxWidth(Double.MAX_VALUE);
        }
    }

    /**
     * Validates the form and updates the Create button state.
     */
    private void updateValidation() {
        Button submitButton = (Button) getDialogPane().lookupButton(submitButtonType);
        if (submitButton != null) {
            updateValidation(submitButton);
        }
    }

    /**
     * Updates validation feedback and the enabled state of the Create button.
     *
     * @param submitButton Submit button to update.
     */
    private void updateValidation(Button submitButton) {
        String validationMessage = getValidationMessage();
        validationLabel.setText(validationMessage);
        validationLabel.setVisible(!validationMessage.isEmpty());
        submitButton.setDisable(!validationMessage.isEmpty());
    }

    /**
     * Returns the current validation message for the form.
     *
     * @return Validation message, or an empty string when valid.
     */
    private String getValidationMessage() {
        return TripFormValidator.validate(titleField.getText(), startDatePicker.getValue(),
                endDatePicker.getValue(), earliestPlanDate, latestPlanDate,
                tripBeingEdited.map(Trip::title).orElse(null));
    }

    /**
     * Creates or updates the Trip when the form is valid, or keeps the dialog open on failure.
     *
     * @param event Submit-button action event.
     */
    private void handleSubmit(ActionEvent event) {
        String validationMessage = getValidationMessage();
        if (!validationMessage.isEmpty()) {
            event.consume();
            updateValidation();
            return;
        }
        try {
            createdTrip = tripBeingEdited.isPresent()
                    ? service.editTrip(tripBeingEdited.get().id(), titleField.getText(), startDatePicker.getValue(),
                            endDatePicker.getValue())
                    : service.createTrip(titleField.getText(), startDatePicker.getValue(), endDatePicker.getValue());
        } catch (RepositoryException exception) {
            event.consume();
            validationLabel.setText("The trip could not be saved. Check the database and try again.");
            validationLabel.setVisible(true);
            validationLabel.setManaged(true);
        }
    }
}
