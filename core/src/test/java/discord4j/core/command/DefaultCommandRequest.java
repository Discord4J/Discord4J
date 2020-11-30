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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class DefaultCommandRequest implements CommandRequest {

    private final MessageCreateEvent event;
    private final String command;
    private final String parameters;

    public DefaultCommandRequest(MessageCreateEvent event, String command, String parameters) {
        this.event = event;
        this.command = command;
        this.parameters = parameters;
    }

    @Override
    public MessageCreateEvent event() {
        return event;
    }

    @Override
    public String command() {
        return command;
    }

    @Override
    public String parameters() {
        return parameters;
    }

}
