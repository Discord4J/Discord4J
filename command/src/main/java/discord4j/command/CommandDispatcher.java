package discord4j.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * This represents a command dispatcher. Implementing this allows for custom command resolution logic (for example,
 * making certain providers available only on certain guilds, etc).
 *
 * @see CommandProvider
 */
@FunctionalInterface
public interface CommandDispatcher {

    /**
     * Called to handle the logic of invoking a command based on event context. This is expected to do two things:
     * <ol>
     *     <li>Determine whether a command should be triggered. This can be due to any arbitrary criteria (existence of
     *     a prefix, a specific use sending the message, etc).</li>
     *     <li>Generate hints to allow for naive argument parsing by command providers. These hints are not necessarily
     *     binding, but providers are expected to at least consider them. The hints are composed of the command that the
     *     message author is attempting to execute (single word) and a set of string indices denoting the subsequence of
     *     the message string which contains the command's arguments.</li>
     * </ol>
     *
     * @param event The event context.
     * @param providers The {@link discord4j.command.CommandProvider}s this dispatcher should consider for dispatching.
     * @param errorHandler The error handler to pass to command.
     * @return A mono which emits executed command.
     *
     * @see discord4j.command.CommandProvider#provide(discord4j.core.event.domain.message.MessageCreateEvent, String, int, int)
     */
    Mono<? extends Command> dispatch(MessageCreateEvent event, Set<CommandProvider> providers,
                                     CommandErrorHandler errorHandler);
}
