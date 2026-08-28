package doggo.ui.cli;

import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Creates commands that target one displayed entity by index.
 */
final class IndexedCommandParser {
    private IndexedCommandParser() {
    }

    /**
     * Parses an indexed command and creates the corresponding command object.
     *
     * @param command Complete command text.
     * @param keyword Indexed command keyword.
     * @param entity Entity targeted by the command.
     * @param commandFactory Factory for a valid positive index.
     * @return Parsed command or an appropriate invalid-input command.
     */
    static Command parse(String command, String keyword, IndexedEntity entity,
                         IntFunction<Command> commandFactory) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(keyword);
        Objects.requireNonNull(entity);
        Objects.requireNonNull(commandFactory);

        String[] arguments = command.trim().split("\\s+");
        if (arguments.length != 2) {
            return new InvalidCommand("Usage: " + keyword + " NUMBER");
        }

        String indexText = arguments[1];
        if (!indexText.matches("\\d+")) {
            return new InvalidIndexCommand(keyword, entity);
        }

        try {
            int index = Integer.parseInt(indexText);
            if (index < 1) {
                return new InvalidIndexCommand(keyword, entity);
            }
            return commandFactory.apply(index);
        } catch (NumberFormatException exception) {
            return new InvalidIndexCommand(keyword, entity);
        }
    }
}
