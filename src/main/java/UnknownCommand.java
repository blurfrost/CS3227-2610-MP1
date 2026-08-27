final class UnknownCommand implements Command {
    private final String input;

    UnknownCommand(String input) {
        this.input = input;
    }

    @Override
    public CommandResult execute(CliContext context) {
        String menu = context.session().mode() == CliMode.MAIN
                ? context.formatter().mainMenu()
                : context.organiseMenu();
        if (context.session().mode() == CliMode.TRIP) {
            menu = context.session().selectedTripId()
                    .flatMap(context.service()::getTrip)
                    .map(context::selectedTripView)
                    .orElse(menu);
        }
        String message = context.formatter().error("Unknown command \"" + input + "\".\n" + menu);
        return new CommandResult(message, false);
    }
}
