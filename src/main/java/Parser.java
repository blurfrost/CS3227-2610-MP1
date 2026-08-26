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
        if (command.equalsIgnoreCase("back")) {
            return new BackCommand();
        }
        return new UnknownCommand(command);
    }
}
