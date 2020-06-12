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

import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.ShardInfo;

/**
 * Indicates that a gateway connection has correctly reconnected.
 * <p>
 * This event is dispatched by Discord4J.
 */
public class ReconnectEvent extends GatewayLifecycleEvent {

    private final long currentAttempt;

    public ReconnectEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long currentAttempt) {
        super(gateway, shardInfo);
        this.currentAttempt = currentAttempt;
    }

    /**
     * Gets the current reconnect attempt.
     *
     * @return The current reconnect attempt.
     */
    public long getCurrentAttempt() {
        return currentAttempt;
    }

    @Override
    public String toString() {
        return "Gateway successfully reconnected";
    }
}
