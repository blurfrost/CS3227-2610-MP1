package doggo.ui.cli;

import java.io.PrintWriter;

/**
 * Provides platform-consistent output for the CLI.
 */
final class CliOutput {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private CliOutput() {
    }

    static PrintWriter platformWriter(PrintWriter writer) {
        if (writer instanceof PlatformPrintWriter) {
            return writer;
        }
        return new PlatformPrintWriter(writer);
    }

    /**
     * Normalizes all common line endings to the current platform separator.
     *
     * @param message Message whose line endings are normalized.
     * @return Message using the current platform separator.
     */
    private static String normalizeLineEndings(String message) {
        return String.valueOf(message).replace("\r\n", "\n")
                .replace("\n", LINE_SEPARATOR);
    }

    /**
     * Writes lines using the current platform separator.
     */
    private static final class PlatformPrintWriter extends PrintWriter {
        private final PrintWriter delegate;

        PlatformPrintWriter(PrintWriter writer) {
            super(writer, true);
            this.delegate = writer;
        }

        @Override
        public void println(String message) {
            super.print(normalizeLineEndings(message));
            super.print(LINE_SEPARATOR);
        }

        @Override
        public void println() {
            super.print(LINE_SEPARATOR);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
