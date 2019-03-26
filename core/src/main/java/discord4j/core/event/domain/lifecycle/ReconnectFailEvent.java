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

package discord4j.core.event.domain.lifecycle;

import discord4j.core.DiscordClient;

/**
 * Indicates that a reconnection attempt has failed and a new attempt should be scheduled, in that case, this event
 * will be followed by a {@link ReconnectStartEvent}.
 */
public class ReconnectFailEvent extends GatewayLifecycleEvent {

    private final int currentAttempt;

    public ReconnectFailEvent(DiscordClient client, int currentAttempt) {
        super(client);
        this.currentAttempt = currentAttempt;
    }

    public int getCurrentAttempt() {
        return currentAttempt;
    }

    @Override
    public String toString() {
        return "Gateway reconnect attempt failed";
    }
}
