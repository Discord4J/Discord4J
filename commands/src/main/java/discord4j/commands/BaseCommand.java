package discord4j.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Very simple command abstraction.
 */
@FunctionalInterface
public interface BaseCommand {

    /**
     * Called to execute this command.
     *
     * @param event The event to act as raw context.
     * @return A mono, whose completion signals that this command has been executed.
     *
     * @implNote Be careful to not block, blocking can lead to scalability issues.
     */
    Mono<Void> execute(MessageCreateEvent event);
}
