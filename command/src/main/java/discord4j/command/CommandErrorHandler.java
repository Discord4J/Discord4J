package discord4j.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

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
     *
     * @return A mono, which completes when the error handler has handled the passed exception.
     */
    Mono<Void> handle(MessageCreateEvent context, CommandException error);
}
