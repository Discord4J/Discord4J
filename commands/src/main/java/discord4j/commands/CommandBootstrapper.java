package discord4j.commands;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * The (thread-safe) entry point for actually using commands. To use, simple construct a
 * {@link discord4j.commands.CommandBootstrapper.Config} instance and call
 * {@link discord4j.commands.CommandBootstrapper.Config#strapTo(discord4j.core.DiscordClient)}.
 *
 * To register events, obtain the {@link discord4j.commands.CommandDispatcher} via {@link #getCommandDispatcher()}
 * or create a new {@link discord4j.commands.CommandDispatcher} instance replace the default instance with the new
 * one via {@link #replaceCommandDispatcher(CommandDispatcher)}.
 *
 * @see #detach()
 * @see discord4j.commands.CommandBootstrapper.Config
 * @see discord4j.commands.CommandProvider
 * @see discord4j.commands.CommandDispatcher
 * @see discord4j.commands.CommandErrorHandler
 * @see discord4j.commands.DefaultCommandDispatcher
 */
public final class CommandBootstrapper {

    private final static CommandDispatcher DEFAULT_DISPATCHER = new DefaultCommandDispatcher();
    private final static CommandErrorHandler DEFAULT_ERROR_HANDLER = (context, error) -> {};

    private final Disposable disposable;
    private volatile CommandDispatcher dispatcher;
    private volatile CommandErrorHandler errorHandler;

    private CommandBootstrapper(DiscordClient client,
                                CommandDispatcher dispatcher,
                                CommandErrorHandler errorHandler,
                                Function<Flux<? extends BaseCommand>, Flux<? extends BaseCommand>> interceptor) {
        this.dispatcher = dispatcher;
        this.errorHandler = errorHandler;
        disposable = client.getEventDispatcher()
                           .on(MessageCreateEvent.class)
                           .flatMap(event -> this.dispatcher.dispatch(event, this.errorHandler))
                           .transform(interceptor::apply)
                           .subscribe();
    }

    /**
     * Detaches this {@link CommandBootstrapper} instance from receiving
     * {@link discord4j.core.event.domain.message.MessageCreateEvent}s.
     */
    public void detach() {
        disposable.dispose();
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

    /**
     * The configuration class for {@link discord4j.commands.CommandBootstrapper}.
     */
    public static final class Config {

        private Function<Flux<? extends BaseCommand>, Flux<? extends BaseCommand>> interceptor = Function.identity();
        private CommandDispatcher dispatcher = DEFAULT_DISPATCHER;
        private CommandErrorHandler errorHandler = DEFAULT_ERROR_HANDLER;

        /**
         * Sets the command interceptor. Note: At the point where this is called, the command would've already been
         * dispatched.
         *
         * @param interceptor The interceptor, allows for peeking into the command dispatch stream.
         * @return The same config instance for chaining.
         */
        public Config setInterceptor(Function<Flux<? extends BaseCommand>, Flux<? extends BaseCommand>> interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        /**
         * Sets the {@link discord4j.commands.CommandDispatcher} to use.
         *
         * @param dispatcher The dispatcher to use.
         * @return The same config instance for chaining.
         */
        public Config setDispatcher(CommandDispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        /**
         * Sets the {@link discord4j.commands.CommandErrorHandler} to use.
         *
         * @param errorHandler The error handler to use.
         * @return The same config instance for chaining.
         */
        public Config setErrorHandler(CommandErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Straps this config to a {@link discord4j.core.DiscordClient}, this immediately registers the built instance
         * as a message listener.
         *
         * @param client The client to strap this to.
         * @return The built {@link discord4j.commands.CommandBootstrapper} instance.
         *
         * @see CommandBootstrapper#detach()
         */
        public CommandBootstrapper strapTo(DiscordClient client) {
            return new CommandBootstrapper(client, dispatcher, errorHandler, interceptor);
        }
    }
}
