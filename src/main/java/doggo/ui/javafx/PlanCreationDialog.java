package doggo.ui.javafx;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

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
 * Displays a modal form for creating a Plan in a selected Trip.
 */
final class PlanCreationDialog extends Dialog<Plan> {
    private static final ButtonType ADD_BUTTON_TYPE = new ButtonType(
            "Add plan", ButtonBar.ButtonData.OK_DONE);

    private final DoggoService service;
    private final Trip trip;
    private final TextField destinationField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final TextField timeField = new TextField();
    private final Label validationLabel = new Label();
    private Plan createdPlan;

    /**
     * Creates a modal Plan form owned by the specified window.
     *
     * @param service Application service used to create the Plan.
     * @param trip Trip that will receive the Plan.
     * @param owner Window that owns this dialog.
     */
    PlanCreationDialog(DoggoService service, Trip trip, Window owner) {
        this.service = Objects.requireNonNull(service);
        this.trip = Objects.requireNonNull(trip);
        initOwner(Objects.requireNonNull(owner));
        initModality(javafx.stage.Modality.WINDOW_MODAL);
        setTitle("Add a plan");

        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ADD_BUTTON_TYPE, ButtonType.CANCEL);
        dialogPane.setContent(createForm());
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(PlanCreationDialog.class.getResource("doggo.css")).toExternalForm());

        Button addButton = (Button) dialogPane.lookupButton(ADD_BUTTON_TYPE);
        addButton.setDefaultButton(true);
        addButton.addEventFilter(ActionEvent.ACTION, this::handleAdd);
        setResultConverter(buttonType -> buttonType == ADD_BUTTON_TYPE ? createdPlan : null);
        setOnShown(event -> destinationField.requestFocus());
        updateValidation(addButton);
    }

    /**
     * Creates the Plan form content with a date range restricted to the Trip.
     *
     * @return Form content.
     */
    private VBox createForm() {
        destinationField.setPromptText("e.g. Senso-ji Temple");
        datePicker.setValue(service.getCurrentDate());
        datePicker.setEditable(false);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean isEmpty) {
                super.updateItem(date, isEmpty);
                setDisable(isEmpty || date.isBefore(trip.startDate()) || date.isAfter(trip.endDate()));
            }
        });
        timeField.setPromptText("HH:mm, e.g. 09:00");
        addFormStyle(destinationField);
        addFormStyle(datePicker);
        addFormStyle(timeField);

        GridPane fields = new GridPane();
        fields.setHgap(14);
        fields.setVgap(12);
        fields.setPadding(new Insets(6, 0, 0, 0));
        ColumnConstraints labelColumn = new ColumnConstraints();
        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);
        fields.getColumnConstraints().addAll(labelColumn, inputColumn);
        addField(fields, 0, "Destination", destinationField);
        addField(fields, 1, "Date", datePicker);
        addField(fields, 2, "Time", timeField);

        validationLabel.getStyleClass().add("form-error");
        validationLabel.setWrapText(true);

        destinationField.textProperty().addListener((observable, oldValue, newValue) -> updateValidation());
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateValidation());
        timeField.textProperty().addListener((observable, oldValue, newValue) -> updateValidation());

        Label introduction = new Label("Add an itinerary stop to " + trip.title() + ".");
        introduction.getStyleClass().add("form-intro");
        VBox form = new VBox(10, introduction, fields, validationLabel);
        form.getStyleClass().add("trip-form");
        form.setPadding(new Insets(8, 10, 4, 10));
        return form;
    }

    /**
     * Applies the shared form-field style to a control.
     *
     * @param field Control receiving the form-field style.
     */
    private static void addFormStyle(Node field) {
        field.getStyleClass().add("form-field");
    }

    /**
     * Adds one labelled field to the Plan form grid.
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
     * Updates validation feedback and the enabled state of the Add button.
     */
    private void updateValidation() {
        Button addButton = (Button) getDialogPane().lookupButton(ADD_BUTTON_TYPE);
        if (addButton != null) {
            updateValidation(addButton);
        }
    }

    /**
     * Updates validation feedback and the enabled state of the Add button.
     *
     * @param addButton Add button to update.
     */
    private void updateValidation(Button addButton) {
        String validationMessage = getValidationMessage();
        validationLabel.setText(validationMessage);
        validationLabel.setVisible(!validationMessage.isEmpty());
        validationLabel.setManaged(!validationMessage.isEmpty());
        addButton.setDisable(!validationMessage.isEmpty());
    }

    /**
     * Returns the current validation message for the form.
     *
     * @return Validation message, or an empty string when valid.
     */
    private String getValidationMessage() {
        return PlanFormValidator.validate(destinationField.getText(), datePicker.getValue(), timeField.getText(),
                trip.startDate(), trip.endDate());
    }

    /**
     * Creates the Plan when the form is valid, or keeps the dialog open on failure.
     *
     * @param event Add-button action event.
     */
    private void handleAdd(ActionEvent event) {
        String validationMessage = getValidationMessage();
        if (!validationMessage.isEmpty()) {
            event.consume();
            updateValidation();
            return;
        }
        try {
            LocalTime time = PlanFormValidator.parseTime(timeField.getText());
            createdPlan = service.addPlan(trip.id(), destinationField.getText(), datePicker.getValue(), time);
        } catch (RepositoryException exception) {
            event.consume();
            showError("The plan could not be saved. Check the database and try again.");
        } catch (IllegalArgumentException exception) {
            event.consume();
            showError(exception.getMessage());
        }
    }

    /**
     * Shows an inline error while keeping the form open.
     *
     * @param message Error message to display.
     */
    private void showError(String message) {
        validationLabel.setText(message);
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
    }
}
