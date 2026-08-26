final class UnknownCommand implements Command {
    private final String input;

    UnknownCommand(String input) {
        this.input = input;
    }

    @Override
    public CommandResult execute(CliContext context) {
        String message = context.session().mode() == CliMode.MAIN
                ? context.formatter().error("Unknown command \"" + input + "\".\n"
                        + context.formatter().mainMenu())
                : context.formatter().error("Unknown command \"" + input + "\".\n"
                        + context.formatter().organiseMenu(context.service().getTrips()));
        return new CommandResult(message, false);
    }
}
