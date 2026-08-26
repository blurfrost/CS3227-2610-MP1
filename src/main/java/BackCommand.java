final class BackCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        if (context.session().mode() == CliMode.TRIP) {
            context.session().setMode(CliMode.ORGANISE);
            return new CommandResult(context.formatter().organiseMenu(context.service().getTrips()), false);
        }
        context.session().setMode(CliMode.MAIN);
        return new CommandResult(context.formatter().mainMenu(), false);
    }
}
