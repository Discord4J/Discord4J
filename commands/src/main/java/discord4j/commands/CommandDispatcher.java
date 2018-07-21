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
@FunctionalInterface
public interface CommandDispatcher {

    /**
     * Called to handle the logic of invoking a command based on event context.
     *
     * @param event The event context.
     * @param providers The {@link discord4j.commands.CommandProvider}s this dispatcher should consider for dispatching.
     * @param errorHandler The error handler to pass to commands.
     * @return A mono which emits executed commands.
     */
    Mono<? extends BaseCommand> dispatch(MessageCreateEvent event, Set<CommandProvider> providers,
                                         CommandErrorHandler errorHandler);
}
