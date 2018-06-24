package discord4j.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This represents a command dispatcher. Implementing this allows for custom command resolution logic (for example,
 * making certain providers available only on certain guilds, etc).
 *
 * @see CommandProvider
 */
public interface CommandDispatcher {

    /**
     * Called to handle the logic of invoking a command based on event context.
     *
     * @param event The event context.
     * @param errorHandler The error handler to pass to commands.
     * @return A mono which emits executed commands.
     */
    Mono<? extends BaseCommand> dispatch(MessageCreateEvent event, CommandErrorHandler errorHandler);

    /**
     * Gets the command providers registered with this dispatcher.
     *
     * @return The providers. It is expected that this returns an immutable copy of the internal backing set.
     */
    Set<CommandProvider> getCommandProviders();

    /**
     * Called to add a command provider dynamically.
     *
     * @param provider The provider to add.
     * @return A {@link discord4j.commands.CommandDispatcher} instance for chaining, this can be a new instance or
     * the current instance.
     */
    CommandDispatcher addCommandProvider(CommandProvider provider);

    /**
     * Called to add command providers dynamically.
     *
     * @param providers The providers to add.
     * @return A {@link discord4j.commands.CommandDispatcher} instance for chaining, this can be a new instance or
     * the current instance.
     */
    default CommandDispatcher addCommandProviders(Collection<? extends CommandProvider> providers) {
        providers.forEach(this::addCommandProvider);
        return this;
    }

    /**
     * Called to add command providers dynamically.
     *
     * @param providers The providers to add.
     * @return A {@link discord4j.commands.CommandDispatcher} instance for chaining, this can be a new instance or
     * the current instance.
     */
    default CommandDispatcher addCommandProviders(Publisher<? extends CommandProvider> providers) {
        Flux.from(providers).subscribe(this::addCommandProvider);
        return this;
    }

    /**
     * Called to drop a command provider dynamically.
     *
     * @param provider The provider to drop.
     * @return A {@link discord4j.commands.CommandDispatcher} instance for chaining, this can be a new instance or
     * the current instance.
     */
    CommandDispatcher dropCommandProvider(CommandProvider provider);

    /**
     * Called to drop command providers dynamically.
     *
     * @param providers The providers to drop.
     * @return A {@link discord4j.commands.CommandDispatcher} instance for chaining, this can be a new instance or
     * the current instance.
     */
    default CommandDispatcher dropCommandProviders(Collection<? extends CommandProvider> providers) {
        providers.forEach(this::dropCommandProvider);
        return this;
    }

    /**
     * Called to drop command providers dynamically.
     *
     * @param providers The providers to drop.
     * @return A {@link discord4j.commands.CommandDispatcher} instance for chaining, this can be a new instance or
     * the current instance.
     */
    default CommandDispatcher dropCommandProviders(Publisher<? extends CommandProvider> providers) {
        Flux.from(providers).subscribe(this::dropCommandProvider);
        return this;
    }
}
