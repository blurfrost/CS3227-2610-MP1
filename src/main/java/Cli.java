import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

final class Cli {
    private final BufferedReader input;
    private final PrintWriter output;
    private final DoggoService service;
    private final Parser parser = new Parser();
    private final CliSession session = new CliSession();
    private final CliFormatter formatter = new CliFormatter();

    Cli(BufferedReader input, PrintWriter output, DoggoService service) {
        this.input = input;
        this.output = output;
        this.service = service;
    }

    void run() {
        CliPrompter prompter = new CliPrompter(input, output);
        CliContext context = new CliContext(service, session, prompter, formatter, output);
        output.println("---doggo v0.1---");
        output.println(formatter.mainMenu());

        boolean shouldExit = false;
        while (!shouldExit) {
            output.print("> ");
            output.flush();
            String inputLine;
            try {
                inputLine = input.readLine();
            } catch (IOException exception) {
                inputLine = null;
            }
            if (inputLine == null) {
                output.println("\n---\nBye!");
                return;
            }

            CommandResult result = parser.parse(inputLine, session.mode()).execute(context);
            output.println("\n---");
            output.println(result.message());
            shouldExit = result.shouldExit();
        }
    }
}
