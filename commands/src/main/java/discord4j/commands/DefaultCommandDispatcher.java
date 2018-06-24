package discord4j.commands;

import discord4j.commands.exceptions.CommandException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * This is the default {@link discord4j.commands.CommandDispatcher} implementation used for
 * the {@link discord4j.commands.CommandBootstrapper}. This instance is mutable, however it is recommended to not
 * mutate to prevent the usage of expensive synchronization locks.
 *
 * @see discord4j.commands.CommandBootstrapper
 */
public final class DefaultCommandDispatcher implements CommandDispatcher {

    private final Set<CommandProvider> providers;
    private final Object lock = new Object();

    public DefaultCommandDispatcher(Set<CommandProvider> providers) {
        this.providers = providers;
    }

    public DefaultCommandDispatcher() {
        this(new LinkedHashSet<>()); //Ordered to provide deterministic invocation orders
    }

    @Override
    public Mono<? extends BaseCommand> dispatch(MessageCreateEvent event, CommandErrorHandler errorHandler) {
        for (CommandProvider provider : providers) {
            Optional<? extends BaseCommand> cmd = provider.provide(event);
            if (cmd.isPresent()) {
                return Mono.just(cmd.get())
                        .flatMap(c -> c.execute(event).thenReturn(c))
                        .doOnError(CommandException.class, t -> errorHandler.handle(event, t));
            }
        }
        return Mono.empty();
    }

    @Override
    public Set<CommandProvider> getCommandProviders() {
        return Collections.unmodifiableSet(providers);
    }

    @Override
    public CommandDispatcher addCommandProvider(CommandProvider provider) {
        synchronized (lock) {
            providers.add(provider);
        }
        return this;
    }

    @Override
    public CommandDispatcher dropCommandProvider(CommandProvider provider) {
        synchronized (lock) {
            providers.remove(provider);
        }
        return this;
    }

    @Override
    public CommandDispatcher addCommandProviders(Collection<? extends CommandProvider> providers) {
        synchronized (lock) {
            this.providers.addAll(providers);
        }
        return this;
    }

    @Override
    public CommandDispatcher dropCommandProviders(Collection<? extends CommandProvider> providers) {
        synchronized (lock) {
            this.providers.removeAll(providers);
        }
        return this;
    }
}
