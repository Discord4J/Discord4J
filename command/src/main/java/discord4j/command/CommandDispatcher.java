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
     * Called to handle the logic of invoking a command based on an event. This is expected to do two things:
     * <ol>
     *     <li>Determine whether a command should be triggered. This can be due to any arbitrary criteria (existence of
     *     a prefix, a specific use sending the message, etc).</li>
     *     <li>Generate hints to allow for naive argument parsing by command providers. These hints are not necessarily
     *     binding, but providers are expected to at least consider them. The hints are composed of the command that the
     *     message author is attempting to execute (single word) and a set of string indices denoting the subsequence of
     *     the message string which contains the command's arguments.</li>
     * </ol>
     *
     * @param event The event for command execution.
     * @param providers The {@link CommandProvider}s this dispatcher should consider for dispatching.
     * @param errorHandler The error handler to pass to command.
     * @return A flux which emits executed command.
     *
     * @see discord4j.command.CommandProvider#provide(discord4j.core.event.domain.message.MessageCreateEvent, String, int, int)
     */
    Publisher<? extends Command<?>> dispatch(MessageCreateEvent event, Set<CommandProvider<?>> providers,
                                             CommandErrorHandler errorHandler);
}
