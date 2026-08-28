package doggo.ui.cli;

final class UnknownCommand implements Command {
    private final String input;

    UnknownCommand(String input) {
        this.input = input;
    }

    @Override
    public CommandResult execute(CliContext context) {
        String message = context.formatter().error("Unknown command \"" + input + "\".\n"
                + context.refreshCurrentView());
        return new CommandResult(message, false);
    }
}
