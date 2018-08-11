package discord4j.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * This represents a generic command provider.
 */
@FunctionalInterface
public interface CommandProvider {

    /**
     * This is called to provide a matching command based on the event context. This context is composed of the raw
     * event object, the command name (as determined by the {@link discord4j.command.CommandDispatcher}, and the
     * indices which are used to indicate which sub-sequence of the message string should be considered for command
     * execution (this, by convention, excludes the: prefix, command name, etc).
     *
     * @param context The raw event context.
     * @param commandName The expected command name parsed.
     * @param commandStartIndex The start index (inclusive) of where naive parsing of arguments should occur.
     * @param commandEndIndex The end index (exclusive) of where naive parsing of arguments should occur.
     * @return The matched command based on the context, or empty if no command matched.
     */
    Mono<? extends Command> provide(MessageCreateEvent context,
                                    String commandName,
                                    int commandStartIndex,
                                    int commandEndIndex);
}
