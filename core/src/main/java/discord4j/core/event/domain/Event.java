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

package discord4j.core.event.domain;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayAggregate;
import discord4j.gateway.ShardInfo;

/**
 * Represents a Discord real-time event used to track a client's state.
 */
public abstract class Event {

    private final GatewayAggregate gateway;
    private final ShardInfo shardInfo;

    protected Event(GatewayAggregate gateway, ShardInfo shardInfo) {
        this.gateway = gateway;
        this.shardInfo = shardInfo;
    }

    public GatewayAggregate getGateway() {
        return gateway;
    }

    public ShardInfo getShardInfo() {
        return shardInfo;
    }

    /**
     * Get the {@link DiscordClient} that emitted this {@link Event}.
     *
     * @return The client emitting this event.
     */
    public DiscordClient getClient() {
        return gateway.getDiscordClient();
    }
}
