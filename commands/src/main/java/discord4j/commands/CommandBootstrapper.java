package discord4j.commands;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;

/**
 * The (thread-safe) entry point for actually using commands. To use, simply instantiate and call {@link #attach()}.
 *
 * To register events, obtain the {@link discord4j.commands.CommandDispatcher} via {@link #getCommandDispatcher()}
 * or create a new {@link discord4j.commands.CommandDispatcher} instance replace the default instance with the new
 * one via {@link #replaceCommandDispatcher(CommandDispatcher)}.
 *
 * @see #attach()
 * @see discord4j.commands.CommandProvider
 * @see discord4j.commands.CommandDispatcher
 * @see discord4j.commands.CommandErrorHandler
 * @see discord4j.commands.DefaultCommandDispatcher
 */
public final class CommandBootstrapper {

    private final static CommandDispatcher DEFAULT_DISPATCHER = new DefaultCommandDispatcher();
    private final static CommandErrorHandler DEFAULT_ERROR_HANDLER = (context, error) -> {};

    private final DiscordClient client;
    private volatile CommandDispatcher dispatcher = DEFAULT_DISPATCHER;
    private volatile CommandErrorHandler errorHandler = DEFAULT_ERROR_HANDLER;

    /**
     * Constructs and injects the message listener for commands.
     *
     * @see #attach()
     */
    public CommandBootstrapper(DiscordClient client) {
        this.client = client;
    }

    /**
     * Attaches this {@link CommandBootstrapper} instance to the
     * {@link discord4j.core.event.domain.message.MessageCreateEvent} of the passed in client.
     *
     * @return A flux (that need not be subscribed to), which signals the completion of command executions.
     */
    public Flux<? extends BaseCommand> attach() {
        Flux<? extends BaseCommand> f = client.getEventDispatcher()
                                              .on(MessageCreateEvent.class)
                                              .flatMap(event -> dispatcher.dispatch(event, errorHandler))
                                              .share(); //Allows for multiple subscribes
        f.subscribe(); //Initial subscribe required to allow for handling without subscriptions
        return f;
    }

    /**
     * Replaces the {@link CommandDispatcher} used by this instance. This can be used to implement
     * custom command discovery rules.
     *
     * @param dispatcher The new dispatcher to use.
     * @return The same {@link CommandBootstrapper} instance to chain.
     */
    public CommandBootstrapper replaceCommandDispatcher(CommandDispatcher dispatcher) {
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
    public CommandBootstrapper replaceCommandErrorHandler(CommandErrorHandler handler) {
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
}
