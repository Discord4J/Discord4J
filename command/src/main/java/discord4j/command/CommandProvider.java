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

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;

/**
 * This represents a generic command provider.
 */
@FunctionalInterface
public interface CommandProvider<T> {

    /**
     * This is called to provide a matching command based on the event context. This context is composed of the raw
     * event object, the command name (as determined by the {@link discord4j.command.CommandDispatcher}, and the
     * indices which are used to indicate which sub-sequence of the message string should be considered for command
     * execution (this, by convention, excludes the: prefix, command name, etc).
     *
     * @param context The raw event context.
     * @param commandName The expected command name parsed. <b>Note:</b> this is expected to be a single word only!
     * @param commandStartIndex The start index (inclusive) of where naive parsing of arguments should occur relative to
     *      the string held in the sent message.
     * @param commandEndIndex The end index (exclusive) of where naive parsing of arguments should occur relative to
     *      the string held in the sent message.
     * @return The matched command based on the context, or empty if no command matched.
     *
     * @see discord4j.command.CommandDispatcher#dispatch(discord4j.core.event.domain.message.MessageCreateEvent, java.util.Set, CommandErrorHandler)
     */
    Publisher<ProviderContext<T>> provide(MessageCreateEvent context,
                                          String commandName,
                                          int commandStartIndex,
                                          int commandEndIndex);
}
