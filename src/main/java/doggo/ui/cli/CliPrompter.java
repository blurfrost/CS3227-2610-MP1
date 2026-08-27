package doggo.ui.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

final class CliPrompter {
    private final BufferedReader input;
    private final PrintWriter output;

    CliPrompter(BufferedReader input, PrintWriter output) {
        this.input = input;
        this.output = output;
    }

    String prompt(String message) {
        output.println("---");
        output.println(message);
        output.print("> ");
        output.flush();
        try {
            return input.readLine();
        } catch (IOException exception) {
            return null;
        }
    }
}
