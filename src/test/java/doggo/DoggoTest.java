package doggo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DoggoTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void run_sameDatabaseAcrossSessions_persistsAndDisplaysTrip() {
        Path databasePath = temporaryDirectory.resolve("doggo.db");
        StringWriter firstOutput = new StringWriter();
        StringWriter firstError = new StringWriter();

        Doggo.run(databasePath, input("new\nPersistent trip\n01/01/2027\n09/01/2027\nexit\n"),
                new PrintWriter(firstOutput, true), new PrintWriter(firstError, true), TestClock.fixed());

        StringWriter secondOutput = new StringWriter();
        StringWriter secondError = new StringWriter();
        Doggo.run(databasePath, input("organise\nexit\n"),
                new PrintWriter(secondOutput, true), new PrintWriter(secondError, true), TestClock.fixed());

        assertTrue(Files.exists(databasePath));
        assertTrue(firstOutput.toString().contains("Trip successfully added!"));
        assertTrue(secondOutput.toString().contains("Persistent trip (from 01/01/2027 to 09/01/2027)"));
        assertTrue(firstError.toString().isEmpty());
        assertTrue(secondError.toString().isEmpty());
    }

    @Test
    void run_unusableDatabasePath_reportsActionableErrorWithoutLaunchingCli() throws IOException {
        Path blockingFile = temporaryDirectory.resolve("blocking-file");
        Files.writeString(blockingFile, "not a directory");
        Path databasePath = blockingFile.resolve("doggo.db");
        StringWriter output = new StringWriter();
        StringWriter error = new StringWriter();

        Doggo.run(databasePath, input("exit\n"), new PrintWriter(output, true),
                new PrintWriter(error, true), TestClock.fixed());

        assertTrue(output.toString().isEmpty());
        assertTrue(error.toString().contains("Unable to start doggo."));
        assertTrue(error.toString().contains("database location is accessible"));
        assertFalse(error.toString().contains("RepositoryException"));
        assertFalse(error.toString().contains("org.sqlite"));
        assertFalse(error.toString().contains("\n\tat "));
    }

    private static BufferedReader input(String value) {
        return new BufferedReader(new StringReader(value));
    }
}
