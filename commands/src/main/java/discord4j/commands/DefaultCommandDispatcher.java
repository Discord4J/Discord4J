package discord4j.commands;

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

    @Override
    public Mono<? extends Command> dispatch(MessageCreateEvent event, Set<CommandProvider> providers,
                                            CommandErrorHandler errorHandler) {
        for (CommandProvider provider : providers) {
            Optional<? extends Command> cmd = provider.provide(event);
            if (cmd.isPresent()) {
                return Mono.just(cmd.get())
                        .flatMap(c -> c.execute(event).thenReturn(c))
                        .doOnError(CommandException.class, t -> errorHandler.handle(event, t));
            }
        }
        return Mono.empty();
    }
}
