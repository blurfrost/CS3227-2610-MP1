package doggo.ui.cli;

/**
 * Reports an invalid index using the currently displayed entity snapshot.
 */
final class InvalidIndexCommand implements Command {
    private final String action;
    private final IndexedEntity entity;

    InvalidIndexCommand(String action, IndexedEntity entity) {
        this.action = action;
        this.entity = entity;
    }

    @Override
    public CommandResult execute(CliContext context) {
        int displayedCount = entity == IndexedEntity.TRIP
                ? context.session().displayedTripCount()
                : context.session().displayedPlanCount();
        return new CommandResult(context.formatter().error(
                context.formatter().invalidIndex(action, entity, displayedCount)
                        + "\n" + context.refreshCurrentView()), false);
    }
}
