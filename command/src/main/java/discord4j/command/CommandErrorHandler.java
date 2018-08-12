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
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * An interface called when an error is encountered.
 *
 * @see CommandException
 */
@FunctionalInterface
public interface CommandErrorHandler {

    /**
     * Called when an error is encountered.
     *
     * @param event The event of where this error occurred.
     * @param error The error encountered. It should be noted that this throwable will be a subclass of
     * {@link CommandException} if an error was due to end-user error, and not an internal issue. As
     * a result it will usually contain a user-friendly message which can simply be reported to the author of the
     * message attempting to invoke a command.
     */
    Mono<Void> handle(MessageCreateEvent event, Throwable error);
}
