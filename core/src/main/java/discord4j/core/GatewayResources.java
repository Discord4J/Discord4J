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

package discord4j.core;

import discord4j.core.event.EventDispatcher;
import discord4j.core.shard.ShardCoordinator;
import discord4j.gateway.GatewayClient;

/**
 * A set of dependencies required to build and coordinate multiple {@link GatewayClient} instances.
 */
public class GatewayResources {

    private final StateHolder stateHolder;
    private final EventDispatcher eventDispatcher;
    private final ShardCoordinator shardCoordinator;

    public GatewayResources(StateHolder stateHolder, EventDispatcher eventDispatcher,
                            ShardCoordinator shardCoordinator) {
        this.stateHolder = stateHolder;
        this.eventDispatcher = eventDispatcher;
        this.shardCoordinator = shardCoordinator;
    }

    public StateHolder getStateHolder() {
        return stateHolder;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public ShardCoordinator getShardCoordinator() {
        return shardCoordinator;
    }
}
