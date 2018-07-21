package discord4j.commands;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The (thread-safe) entry point for actually using commands. To use, simply instantiate and call
 * {@link #attach(discord4j.core.DiscordClient)}.
 *
 * To register events, obtain the {@link discord4j.commands.CommandDispatcher} via {@link #getCommandDispatcher()}
 * or create a new {@link discord4j.commands.CommandDispatcher} instance replace the default instance with the new
 * one via {@link #setCommandDispatcher(CommandDispatcher)}.
 *
 * @see #attach(discord4j.core.DiscordClient)
 * @see discord4j.commands.CommandProvider
 * @see discord4j.commands.CommandDispatcher
 * @see discord4j.commands.CommandErrorHandler
 * @see discord4j.commands.DefaultCommandDispatcher
 */
public final class CommandBootstrapper {

    private final static CommandDispatcher DEFAULT_DISPATCHER = new DefaultCommandDispatcher();
    private final static CommandErrorHandler DEFAULT_ERROR_HANDLER = (context, error) -> {};

    private volatile CommandDispatcher dispatcher = DEFAULT_DISPATCHER;
    private volatile CommandErrorHandler errorHandler = DEFAULT_ERROR_HANDLER;

    private final Set<CommandProvider> providers;

    /**
     * Constructs the message listener for commands.
     *
     * @see #attach(discord4j.core.DiscordClient)
     * @param providers The {@link discord4j.commands.CommandProvider}s to initialize this instance with. Note that
     * this set is expected to be thread-safe as there will be concurrent read access required.
     */
    public CommandBootstrapper(Set<CommandProvider> providers) {
        this.providers = providers;
    }

    /**
     * Constructs the message listener for commands.
     *
     * @see #attach(discord4j.core.DiscordClient)
     */
    public CommandBootstrapper() {
        this(new CopyOnWriteArraySet<>()); //TODO: LinkedHashSet is probably better, but not concurrent!
    }

    /**
     * Attaches this {@link CommandBootstrapper} instance to the
     * {@link discord4j.core.event.domain.message.MessageCreateEvent} of the passed in client.
     *
     * @return A flux (that need not be subscribed to), which signals the completion of command executions.
     */
    public Flux<? extends Command> attach(DiscordClient client) {
        return client.getEventDispatcher()
                     .on(MessageCreateEvent.class)
                     .flatMap(event -> dispatcher.dispatch(event, providers, errorHandler))
                     .share();
    }

    /**
     * Replaces the {@link CommandDispatcher} used by this instance. This can be used to implement
     * custom command discovery rules.
     *
     * @param dispatcher The new dispatcher to use.
     * @return The same {@link CommandBootstrapper} instance to chain.
     */
    public CommandBootstrapper setCommandDispatcher(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        return this;
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
     * @return The current {@link discord4j.commands.CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper addCommandProvider(CommandProvider provider) {
        providers.add(provider);
        return this;
    }

    /**
     * Called to drop a command provider dynamically.
     *
     * @param provider The provider to remove.
     * @return The current {@link discord4j.commands.CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper removeCommandProvider(CommandProvider provider) {
        providers.remove(provider);
        return this;
    }

    /**
     * Called to add command providers dynamically.
     *
     * @param providers The providers to add.
     * @return The current {@link discord4j.commands.CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper addCommandProviders(Collection<? extends CommandProvider> providers) {
        this.providers.addAll(providers);
        return this;
    }

    /**
     * Called to remove command providers dynamically.
     *
     * @param providers The providers to remove.
     * @return The current {@link discord4j.commands.CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper removeCommandProviders(Collection<? extends CommandProvider> providers) {
        this.providers.removeAll(providers);
        return this;
    }
}
