package doggo.ui.cli;

/**
 * Enters the Gallery mode.
 */
final class GalleryCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        context.session().enterGallery();
        return new CommandResult(context.galleryMenu(), false);
    }
}
