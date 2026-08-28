package doggo.ui.cli;

final class UnknownCommand implements Command {
    private final String input;

    UnknownCommand(String input) {
        this.input = input;
    }

    @Override
    public CommandResult execute(CliContext context) {
        String menu = switch (context.session().mode()) {
        case MAIN -> context.formatter().mainMenu();
        case ORGANISE -> context.organiseMenu();
        case DASHBOARD -> context.dashboardMenu();
        case TRIP -> context.session().selectedTripId()
                .flatMap(context.service()::getTrip)
                .map(context::selectedTripView)
                .orElseGet(context::organiseMenu);
        };
        String message = context.formatter().error("Unknown command \"" + input + "\".\n" + menu);
        return new CommandResult(message, false);
    }
}
