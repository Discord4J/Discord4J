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

package discord4j.core.event.domain.lifecycle;

import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.ShardInfo;

/**
 * Indicates that a gateway connection is starting a reconnect attempt. Can be followed by {@link ReconnectEvent} if
 * successful, or {@link ReconnectFailEvent} if not.
 * <p>
 * This event is dispatched by Discord4J.
 */
public class ReconnectStartEvent extends GatewayLifecycleEvent {

    public ReconnectStartEvent(GatewayDiscordClient gateway, ShardInfo shardInfo) {
        super(gateway, shardInfo);
    }

    @Override
    public String toString() {
        return "Gateway reconnect attempt started";
    }
}
