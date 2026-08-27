final class InvalidCommand implements Command {
    private final String usage;

    InvalidCommand(String usage) {
        this.usage = usage;
    }

    @Override
    public CommandResult execute(CliContext context) {
        String view = context.session().mode() == CliMode.TRIP
                ? context.session().selectedTripId()
                        .flatMap(context.service()::getTrip)
                        .map(context::selectedTripView)
                        .orElseGet(() -> {
                            context.session().enterOrganise();
                            return context.organiseMenu();
                        })
                : context.organiseMenu();
        return new CommandResult(context.formatter().error(
                usage + "\n" + view), false);
    }
}
