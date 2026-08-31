package doggo.ui.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.domain.Review;
import doggo.domain.Trip;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Collects an optional rating and Notes for a Trip or Plan Review.
 *
 * @param <T> Reviewed domain value returned after persistence.
 */
final class ReviewDialog<T> extends Dialog<T> {
    private static final int MINIMUM_RATING = 1;
    private static final int MAXIMUM_RATING = 5;

    private final T originalTarget;
    private final Optional<Review> existingReview;
    private final ReviewSaver<T> reviewSaver;
    private final ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    private final List<ToggleButton> ratingButtons = new ArrayList<>();
    private final TextArea notesArea = new TextArea();
    private final Label errorLabel = new Label();

    private T savedTarget;

    /**
     * Returns a Review dialog configured for the specified Trip.
     *
     * @param service Application service used to persist the Review.
     * @param trip Trip being reviewed.
     * @param owner Window that owns this dialog.
     * @return Configured Trip Review dialog.
     */
    static ReviewDialog<Trip> forTrip(DoggoService service, Trip trip, Window owner) {
        Objects.requireNonNull(service);
        Objects.requireNonNull(trip);
        return new ReviewDialog<>("Review a trip",
                "Add an overall rating or a few notes about \"" + trip.title() + "\".",
                trip, trip.review(), review -> saveTripReview(service, trip, review), owner);
    }

    /**
     * Creates a reusable Review form with target-specific persistence.
     *
     * @param title Dialog title.
     * @param introductionText Introductory form text.
     * @param originalTarget Original reviewed value.
     * @param existingReview Existing Review, if one is recorded.
     * @param reviewSaver Persistence operation for a submitted Review.
     * @param owner Window that owns this dialog.
     */
    private ReviewDialog(String title, String introductionText, T originalTarget,
                         Optional<Review> existingReview, ReviewSaver<T> reviewSaver, Window owner) {
        this.originalTarget = Objects.requireNonNull(originalTarget);
        this.existingReview = Objects.requireNonNull(existingReview);
        this.reviewSaver = Objects.requireNonNull(reviewSaver);
        initOwner(Objects.requireNonNull(owner));
        initModality(Modality.WINDOW_MODAL);
        setTitle(Objects.requireNonNull(title));

        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialogPane.setContent(createForm(Objects.requireNonNull(introductionText)));
        dialogPane.setPrefWidth(500);
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(ReviewDialog.class.getResource("doggo.css")).toExternalForm());

        Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
        saveButton.setDefaultButton(true);
        saveButton.addEventFilter(ActionEvent.ACTION, this::handleSave);
        setResultConverter(buttonType -> buttonType == saveButtonType ? savedTarget : null);
        DialogWindowSupport.enableExpandOnly(this, notesArea);
    }

    /**
     * Creates the rating and Notes form with any existing Review values.
     *
     * @param introductionText Introductory text shown above the fields.
     * @return Review form content.
     */
    private VBox createForm(String introductionText) {
        Label introduction = new Label(introductionText);
        introduction.getStyleClass().add("form-intro");
        introduction.setWrapText(true);

        Label ratingLabel = new Label("Rating");
        ratingLabel.getStyleClass().add("form-label");
        HBox ratingChoices = createRatingChoices();

        Label notesLabel = new Label("Notes");
        notesLabel.getStyleClass().add("form-label");
        notesArea.setId("reviewNotesField");
        notesArea.setPromptText("What stood out about this trip?");
        notesArea.setPrefRowCount(6);
        notesArea.setWrapText(true);
        notesArea.getStyleClass().addAll("form-field", "review-notes-field");

        errorLabel.setId("reviewErrorLabel");
        errorLabel.getStyleClass().add("form-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setWrapText(true);

        prefillExistingReview();
        VBox form = new VBox(8, introduction, ratingLabel, ratingChoices, notesLabel, notesArea, errorLabel);
        form.getStyleClass().add("review-form");
        form.setPadding(new Insets(8, 10, 4, 10));
        VBox.setVgrow(notesArea, Priority.ALWAYS);
        return form;
    }

    /**
     * Creates five mutually exclusive rating buttons that allow no selection.
     *
     * @return Rating-button row.
     */
    private HBox createRatingChoices() {
        HBox ratingChoices = new HBox(8);
        ratingChoices.getStyleClass().add("rating-choices");
        for (int rating = MINIMUM_RATING; rating <= MAXIMUM_RATING; rating++) {
            ToggleButton ratingButton = createRatingButton(rating);
            ratingButtons.add(ratingButton);
            ratingChoices.getChildren().add(ratingButton);
        }
        return ratingChoices;
    }

    /**
     * Creates one numeric rating button.
     *
     * @param rating Rating represented by the button.
     * @return Configured rating button.
     */
    private ToggleButton createRatingButton(int rating) {
        ToggleButton ratingButton = new ToggleButton(Integer.toString(rating));
        ratingButton.setId("ratingButton" + rating);
        ratingButton.setAccessibleText("Rating " + rating + " out of 5");
        ratingButton.getStyleClass().add("rating-button");
        ratingButton.setOnAction(event -> {
            if (ratingButton.isSelected()) {
                ratingButtons.stream()
                        .filter(otherButton -> otherButton != ratingButton)
                        .forEach(otherButton -> otherButton.setSelected(false));
            }
        });
        return ratingButton;
    }

    /**
     * Loads an existing rating and Notes into the form.
     */
    private void prefillExistingReview() {
        existingReview.ifPresent(review -> {
            review.rating().ifPresent(rating -> ratingButtons.get(rating - MINIMUM_RATING).setSelected(true));
            notesArea.setText(review.text().orElse(""));
        });
    }

    /**
     * Persists the submitted Review or keeps the dialog open when saving fails.
     *
     * @param event Save-button action event.
     */
    private void handleSave(ActionEvent event) {
        try {
            Optional<Review> submittedReview = createSubmittedReview();
            savedTarget = submittedReview.equals(existingReview)
                    ? originalTarget
                    : reviewSaver.save(submittedReview);
        } catch (RepositoryException exception) {
            event.consume();
            showError("The review could not be saved. Check the database and try again.");
        } catch (IllegalArgumentException exception) {
            event.consume();
            showError(exception.getMessage());
        }
    }

    /**
     * Returns the Review represented by the optional form fields.
     *
     * @return Submitted Review, or empty when both fields are absent.
     */
    private Optional<Review> createSubmittedReview() {
        OptionalInt rating = ratingButtons.stream()
                .filter(ToggleButton::isSelected)
                .findFirst()
                .map(button -> OptionalInt.of(Integer.parseInt(button.getText())))
                .orElse(OptionalInt.empty());
        Optional<String> notes = Optional.of(notesArea.getText().trim())
                .filter(value -> !value.isBlank());
        if (rating.isEmpty() && notes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Review(rating, notes));
    }

    /**
     * Displays an inline persistence or validation error.
     *
     * @param message Error message to display.
     */
    private void showError(String message) {
        errorLabel.setText(message == null ? "The review could not be saved." : message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Saves or removes a Trip Review according to the submitted form state.
     *
     * @param service Application service used to persist the Trip.
     * @param trip Trip being reviewed.
     * @param review Submitted Review, or empty to remove it.
     * @return Updated Trip.
     */
    private static Trip saveTripReview(DoggoService service, Trip trip, Optional<Review> review) {
        return review.isPresent()
                ? service.setTripReview(trip.id(), review.orElseThrow())
                : service.removeTripReview(trip.id());
    }

    /**
     * Persists an optional Review for a configured domain target.
     *
     * @param <T> Reviewed domain value returned after persistence.
     */
    @FunctionalInterface
    private interface ReviewSaver<T> {
        /**
         * Returns the reviewed value after saving the submitted Review state.
         *
         * @param review Submitted Review, or empty to remove it.
         * @return Updated reviewed value.
         */
        T save(Optional<Review> review);
    }
}
