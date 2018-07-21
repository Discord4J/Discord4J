package discord4j.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * This represents a generic command provider.
 */
@FunctionalInterface
public interface CommandProvider {

    /**
     * This is called to provide a matching command based on the event context.
     *
     * @param context The raw event context.
     * @return The matched command based on the context, or empty if no command matched.
     */
    Mono<? extends Command> provide(MessageCreateEvent context);
}
