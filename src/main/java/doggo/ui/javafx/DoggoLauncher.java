package doggo.ui.javafx;

import javafx.application.Application;

/**
 * Launches the doggo JavaFX application from the classpath.
 */
public final class DoggoLauncher {
    private DoggoLauncher() {
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args JavaFX launch arguments.
     */
    public static void main(String[] args) {
        Application.launch(DoggoApplication.class, args);
    }
}
