package doggo.ui.javafx;

import java.time.LocalDate;
import java.util.Objects;

import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.domain.Trip;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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
 * Displays a modal form for creating a Trip.
 */
final class TripCreationDialog extends Dialog<Trip> {
    private static final ButtonType CREATE_BUTTON_TYPE = new ButtonType(
            "Create trip", ButtonBar.ButtonData.OK_DONE);

    private final DoggoService service;
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
        this.service = Objects.requireNonNull(service);
        initOwner(Objects.requireNonNull(owner));
        initModality(javafx.stage.Modality.WINDOW_MODAL);
        setTitle("Create a trip");

        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(CREATE_BUTTON_TYPE, ButtonType.CANCEL);
        dialogPane.setContent(createForm());
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(TripCreationDialog.class.getResource("doggo.css")).toExternalForm());

        Button createButton = (Button) dialogPane.lookupButton(CREATE_BUTTON_TYPE);
        createButton.setDefaultButton(true);
        createButton.addEventFilter(ActionEvent.ACTION, this::handleCreate);
        setResultConverter(buttonType -> buttonType == CREATE_BUTTON_TYPE ? createdTrip : null);
        setOnShown(event -> titleField.requestFocus());
        updateValidation(createButton);
    }

    /**
     * Creates the Trip form content with default dates from the service Clock.
     *
     * @return Form content.
     */
    private VBox createForm() {
        LocalDate currentDate = service.getCurrentDate();
        titleField.setPromptText("e.g. A week in Japan");
        startDatePicker.setValue(currentDate);
        endDatePicker.setValue(currentDate);
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

        titleField.textProperty().addListener((observable, oldValue, newValue) -> updateValidation());
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateValidation());
        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateValidation());

        Label introduction = new Label("Give your next journey a name and a date range to get started.");
        introduction.getStyleClass().add("form-intro");
        VBox form = new VBox(10, introduction, fields, validationLabel);
        form.getStyleClass().add("trip-form");
        form.setPadding(new Insets(8, 10, 4, 10));
        return form;
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
        Button createButton = (Button) getDialogPane().lookupButton(CREATE_BUTTON_TYPE);
        if (createButton != null) {
            updateValidation(createButton);
        }
    }

    /**
     * Updates validation feedback and the enabled state of the Create button.
     *
     * @param createButton Create button to update.
     */
    private void updateValidation(Button createButton) {
        String validationMessage = getValidationMessage();
        validationLabel.setText(validationMessage);
        validationLabel.setVisible(!validationMessage.isEmpty());
        validationLabel.setManaged(!validationMessage.isEmpty());
        createButton.setDisable(!validationMessage.isEmpty());
    }

    /**
     * Returns the current validation message for the form.
     *
     * @return Validation message, or an empty string when valid.
     */
    private String getValidationMessage() {
        return TripFormValidator.validate(titleField.getText(), startDatePicker.getValue(),
                endDatePicker.getValue());
    }

    /**
     * Creates the Trip when the form is valid, or keeps the dialog open on failure.
     *
     * @param event Create-button action event.
     */
    private void handleCreate(ActionEvent event) {
        String validationMessage = getValidationMessage();
        if (!validationMessage.isEmpty()) {
            event.consume();
            updateValidation();
            return;
        }
        try {
            createdTrip = service.createTrip(titleField.getText(), startDatePicker.getValue(),
                    endDatePicker.getValue());
        } catch (RepositoryException exception) {
            event.consume();
            validationLabel.setText("The trip could not be saved. Check the database and try again.");
            validationLabel.setVisible(true);
            validationLabel.setManaged(true);
        }
    }
}
