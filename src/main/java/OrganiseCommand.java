final class OrganiseCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        context.session().setMode(CliMode.ORGANISE);
        return new CommandResult(context.formatter().organiseMenu(context.service().getTrips()), false);
    }
}
