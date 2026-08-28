package doggo.ui.cli;

final class Parser {
    private final ModeCommandParser mainCommandParser = new MainCommandParser();
    private final ModeCommandParser organiseCommandParser = new OrganiseCommandParser();
    private final ModeCommandParser tripCommandParser = new TripCommandParser();
    private final ModeCommandParser dashboardCommandParser = new DashboardCommandParser();

    Command parse(String input, CliMode mode) {
        String command = input == null ? "" : input.trim();
        if (command.equalsIgnoreCase("exit")) {
            return new ExitCommand();
        }
        if (command.equalsIgnoreCase("back")) {
            return new BackCommand();
        }
        if (mode == null) {
            return new UnknownCommand(command);
        }

        return switch (mode) {
            case MAIN -> mainCommandParser.parse(command);
            case ORGANISE -> organiseCommandParser.parse(command);
            case TRIP -> tripCommandParser.parse(command);
            case DASHBOARD -> dashboardCommandParser.parse(command);
        };
    }
}
