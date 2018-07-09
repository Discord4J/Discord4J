package discord4j.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * An interface called when a {@link CommandException} error is encountered.
 *
 * @see CommandException
 */
@FunctionalInterface
public interface CommandErrorHandler {

    /**
     * Called when an error is encountered.
     *
     * @param context The context of where this error occurred.
     * @param error The error encountered.
     */
    void handle(MessageCreateEvent context, CommandException error);
}
