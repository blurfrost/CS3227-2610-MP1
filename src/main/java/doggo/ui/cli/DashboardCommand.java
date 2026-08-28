package doggo.ui.cli;

/**
 * Enters the Dashboard mode.
 */
final class DashboardCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        context.session().enterDashboard();
        return new CommandResult(context.dashboardMenu(), false);
    }
}
