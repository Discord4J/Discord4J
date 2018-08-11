package discord4j.command;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The (thread-safe) entry point for actually using command. To use, simply instantiate and call
 * {@link #attach(discord4j.core.DiscordClient)}.
 *
 * @see #attach(discord4j.core.DiscordClient)
 * @see discord4j.command.CommandProvider
 * @see discord4j.command.CommandDispatcher
 * @see discord4j.command.CommandErrorHandler
 * @see discord4j.command.NaiveCommandDispatcher
 */
public final class CommandBootstrapper {

    private final static Logger log = Loggers.getLogger(CommandBootstrapper.class);
    private final static CommandErrorHandler DEFAULT_ERROR_HANDLER = (context, error) -> Mono.defer(() -> {
        log.warn("Command excecution failed! Reason: {}", error.response().orElse("None"));
        return Mono.empty();
    });

    private volatile CommandErrorHandler errorHandler = DEFAULT_ERROR_HANDLER;

    private final Set<CommandProvider> providers;
    private final CommandDispatcher dispatcher;

    /**
     * Constructs the message listener for command.
     *
     * @param dispatcher The command dispatcher to be used to emit events from.
     * @param providers The {@link discord4j.command.CommandProvider}s to initialize this instance with. Note that
     * this set is expected to be thread-safe as there will be concurrent read access required.
     *
     * @see #attach(discord4j.core.DiscordClient)
     */
    public CommandBootstrapper(CommandDispatcher dispatcher, Set<CommandProvider> providers) {
        this.dispatcher = dispatcher;
        this.providers = providers;
    }

    /**
     * Constructs the message listener for command.
     *
     * @see #attach(discord4j.core.DiscordClient)
     */
    public CommandBootstrapper(CommandDispatcher dispatcher) {
        this(dispatcher, new CopyOnWriteArraySet<>()); //TODO: LinkedHashSet is probably better, but not concurrent!
    }

    /**
     * Attaches this {@link CommandBootstrapper} instance to the
     * {@link discord4j.core.event.domain.message.MessageCreateEvent} of the passed in client.
     *
     * @return A flux which signals the completion of command executions.
     */
    public Flux<? extends Command> attach(DiscordClient client) {
        return client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .flatMap(event -> dispatcher.dispatch(event, providers, errorHandler))
                .share();
    }

    /**
     * Replaces the {@link CommandErrorHandler} used by this instance. This is used to
     * implement unified error handling despite command implementations.
     *
     * @param handler The new error handler to use.
     * @return The same {@link CommandBootstrapper} instance to chain.
     */
    public CommandBootstrapper setCommandErrorHandler(CommandErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
     * Gets the {@link CommandDispatcher} being currently used.
     *
     * @return The current {@link CommandDispatcher}.
     */
    public CommandDispatcher getCommandDispatcher() {
        return dispatcher;
    }

    /**
     * Gets the {@link CommandErrorHandler} being currently used.
     *
     * @return The current {@link CommandErrorHandler}.
     */
    public CommandErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Gets the command providers registered with this dispatcher.
     *
     * @return The providers. It is expected that this returns an immutable copy of the internal backing set.
     */
    public Set<CommandProvider> getCommandProviders() {
        return Collections.unmodifiableSet(providers);
    }

    /**
     * Called to add a command provider dynamically.
     *
     * @param provider The provider to add.
     * @return The current {@link discord4j.command.CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper addCommandProvider(CommandProvider provider) {
        providers.add(provider);
        return this;
    }

    /**
     * Called to drop a command provider dynamically.
     *
     * @param provider The provider to remove.
     * @return The current {@link discord4j.command.CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper removeCommandProvider(CommandProvider provider) {
        providers.remove(provider);
        return this;
    }

    /**
     * Called to add command providers dynamically.
     *
     * @param providers The providers to add.
     * @return The current {@link discord4j.command.CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper addCommandProviders(Collection<? extends CommandProvider> providers) {
        this.providers.addAll(providers);
        return this;
    }

    /**
     * Called to remove command providers dynamically.
     *
     * @param providers The providers to remove.
     * @return The current {@link discord4j.command.CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper removeCommandProviders(Collection<? extends CommandProvider> providers) {
        this.providers.removeAll(providers);
        return this;
    }
}
