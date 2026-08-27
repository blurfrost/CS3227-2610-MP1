package doggo.ui.cli;

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
        if (mode == CliMode.ORGANISE && command.matches("(?i)edit\\s+\\d+")) {
            try {
                return new EditTripCommand(parseIndex(command, "edit"));
            } catch (NumberFormatException exception) {
                return new InvalidCommand("Usage: edit NUMBER");
            }
        }
        if (mode == CliMode.ORGANISE && command.matches("(?i)edit(?:\\s+.*)?")) {
            return new InvalidCommand("Usage: edit NUMBER");
        }
        if (mode == CliMode.ORGANISE && command.matches("(?i)view\\s+\\d+")) {
            try {
                String index = command.substring("view".length()).trim();
                return new ViewTripCommand(Integer.parseInt(index));
            } catch (NumberFormatException exception) {
                return new UnknownCommand(command);
            }
        }
        if (mode == CliMode.ORGANISE && command.matches("(?i)delete\\s+\\d+")) {
            try {
                return new DeleteTripCommand(parseIndex(command, "delete"));
            } catch (NumberFormatException exception) {
                return new InvalidCommand("Usage: delete NUMBER");
            }
        }
        if (mode == CliMode.ORGANISE && command.matches("(?i)delete(?:\\s+.*)?")) {
            return new InvalidCommand("Usage: delete NUMBER");
        }
        if (mode == CliMode.TRIP && command.equalsIgnoreCase("new")) {
            return new NewPlanCommand();
        }
        if (mode == CliMode.TRIP && command.matches("(?i)edit\\s+\\d+")) {
            try {
                return new EditPlanCommand(parseIndex(command, "edit"));
            } catch (NumberFormatException exception) {
                return new InvalidCommand("Usage: edit NUMBER");
            }
        }
        if (mode == CliMode.TRIP && command.matches("(?i)edit(?:\\s+.*)?")) {
            return new InvalidCommand("Usage: edit NUMBER");
        }
        if (mode == CliMode.TRIP && command.matches("(?i)delete\\s+\\d+")) {
            try {
                return new DeletePlanCommand(parseIndex(command, "delete"));
            } catch (NumberFormatException exception) {
                return new InvalidCommand("Usage: delete NUMBER");
            }
        }
        if (mode == CliMode.TRIP && command.matches("(?i)delete(?:\\s+.*)?")) {
            return new InvalidCommand("Usage: delete NUMBER");
        }
        if (command.equalsIgnoreCase("back")) {
            return new BackCommand();
        }
        return new UnknownCommand(command);
    }

    /**
     * Parses and validates a positive one-based command index.
     *
     * @param command Complete command text.
     * @param keyword Command keyword.
     * @return Parsed command index.
     * @throws NumberFormatException If the index is not positive or cannot be parsed.
     */
    private static int parseIndex(String command, String keyword) {
        String index = command.substring(keyword.length()).trim();
        int parsedIndex = Integer.parseInt(index);
        if (parsedIndex < 1) {
            throw new NumberFormatException("Index must be positive.");
        }
        return parsedIndex;
    }
}
