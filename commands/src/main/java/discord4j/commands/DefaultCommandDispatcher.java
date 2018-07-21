package discord4j.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * This is the default {@link discord4j.commands.CommandDispatcher} implementation used for
 * the {@link discord4j.commands.CommandBootstrapper}.
 *
 * @see discord4j.commands.CommandBootstrapper
 */
public final class DefaultCommandDispatcher implements CommandDispatcher {

    @Override
    public Mono<? extends Command> dispatch(MessageCreateEvent event, Set<CommandProvider> providers,
                                            CommandErrorHandler errorHandler) {
        return Flux.fromIterable(providers)
            .concatMap(provider -> provider.provide(event))
            .next()
            .flatMap(cmd -> cmd.execute(event).thenReturn(cmd))
            .doOnError(CommandException.class, throwable -> errorHandler.handle(event, throwable));
    }
}
