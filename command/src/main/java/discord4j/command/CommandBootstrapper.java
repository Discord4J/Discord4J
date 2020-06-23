/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.command;

import discord4j.command.util.CommandException;
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
 * {@link #attach(DiscordClient)}.
 * <p>
 * To register events, obtain the {@link CommandDispatcher} via {@link #getDispatcher()}.
 *
 * @see #attach(DiscordClient)
 * @see CommandProvider
 * @see CommandDispatcher
 * @see CommandErrorHandler
 * @see CommandException
 */
public final class CommandBootstrapper {

    private static final Logger LOGGER = Loggers.getLogger(CommandBootstrapper.class);

    private static final CommandErrorHandler DEFAULT_ERROR_HANDLER = (context, error) -> {
        LOGGER.warn(error.getMessage(), error);
        return Mono.empty();
    };

    private final CommandErrorHandler errorHandler;
    private final Set<CommandProvider<?>> providers;
    private final CommandDispatcher dispatcher;

    /**
     * Constructs the message listener for command.
     *
     * @param dispatcher The command dispatcher to be used to emit events from.
     * @param errorHandler The {@link CommandErrorHandler} for the dispatcher.
     * @see #attach(DiscordClient)
     */
    public CommandBootstrapper(final CommandDispatcher dispatcher, final CommandErrorHandler errorHandler) {
        this.dispatcher = dispatcher;
        this.errorHandler = errorHandler;
        providers = new CopyOnWriteArraySet<>();
    }

    /**
     * Constructs the message listener for command.
     *
     * @see #attach(DiscordClient)
     */
    public CommandBootstrapper(final CommandDispatcher dispatcher) {
        this(dispatcher, DEFAULT_ERROR_HANDLER);
    }

    /**
     * Attaches this {@link CommandBootstrapper} instance to the {@link MessageCreateEvent} of the passed in client.
     *
     * @return A flux (that need not be subscribed to), which signals the completion of command executions.
     */
    public Flux<? extends Command<?>> attach(final DiscordClient client) {
        return client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getContent().isPresent())
                .flatMap(event -> Flux.defer(() -> dispatcher.dispatch(event, providers, errorHandler))
                        .onErrorResume(t -> {
                            LOGGER.warn("Error while dispatching command", t);
                            return Mono.empty();
                        }))
                .share();
    }

    /**
     * Gets the {@link CommandDispatcher} being currently used.
     *
     * @return The current {@link CommandDispatcher}.
     */
    public CommandDispatcher getDispatcher() {
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
    public Set<CommandProvider<?>> getProviders() {
        return Collections.unmodifiableSet(providers);
    }

    /**
     * Called to add a command provider dynamically.
     *
     * @param provider The provider to add.
     * @return The current {@link CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper addProvider(final CommandProvider<?> provider) {
        providers.add(provider);
        return this;
    }

    /**
     * Called to drop a command provider dynamically.
     *
     * @param provider The provider to remove.
     * @return The current {@link CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper removeProvider(final CommandProvider<?> provider) {
        providers.remove(provider);
        return this;
    }

    /**
     * Called to add command providers dynamically.
     *
     * @param providers The providers to add.
     * @return The current {@link CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper addProviders(final Collection<? extends CommandProvider<?>> providers) {
        this.providers.addAll(providers);
        return this;
    }

    /**
     * Called to remove command providers dynamically.
     *
     * @param providers The providers to remove.
     * @return The current {@link CommandBootstrapper} instance for chaining.
     */
    public CommandBootstrapper removeProviders(final Collection<? extends CommandProvider<?>> providers) {
        this.providers.removeAll(providers);
        return this;
    }
}
