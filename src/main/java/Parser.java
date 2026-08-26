final class Parser {
    Command parse(String input, CliMode mode) {
        String command = input == null ? "" : input.trim();
        if (command.equalsIgnoreCase("exit")) {
            return new ExitCommand();
        }
        if (mode == CliMode.MAIN && command.equalsIgnoreCase("organise")) {
            return new OrganiseCommand();
        }
        if (mode == CliMode.ORGANISE && command.equalsIgnoreCase("new")) {
            return new NewTripCommand();
        }
        if (mode == CliMode.ORGANISE && command.matches("\\d+")) {
            try {
                return new SelectTripCommand(Integer.parseInt(command));
            } catch (NumberFormatException exception) {
                return new UnknownCommand(command);
            }
        }
        if (mode == CliMode.TRIP && command.equalsIgnoreCase("new")) {
            return new NewPlanCommand();
        }
        if (command.equalsIgnoreCase("back")) {
            return new BackCommand();
        }
        return new UnknownCommand(command);
    }
}
