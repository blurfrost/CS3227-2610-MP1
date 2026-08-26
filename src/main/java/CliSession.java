final class CliSession {
    private CliMode mode = CliMode.MAIN;

    CliMode mode() {
        return mode;
    }

    void setMode(CliMode mode) {
        this.mode = mode;
    }
}
