package discord4j.commands.exceptions;

import java.util.Optional;

/**
 * An abstract class for propagating non-fatal, user-facing, command-caused errors.
 */
public abstract class CommandException extends RuntimeException {

    public CommandException() {
        //Dummy constructor to prevent expensive stacktrace generation
    }

    /**
     * Returns a human-readable error message for the user who caused this error to read.
     *
     * @return The human-readable error message or empty to not respond.
     */
    public abstract Optional<String> response();
}
