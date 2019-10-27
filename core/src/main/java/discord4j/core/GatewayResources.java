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
import discord4j.core.state.StateView;
import discord4j.gateway.GatewayClient;
import discord4j.store.api.Store;

/**
 * A set of dependencies required to build and coordinate multiple {@link GatewayClient} instances.
 */
public class GatewayResources {

    private final StateView stateView;
    private final EventDispatcher eventDispatcher;
    private final ShardCoordinator shardCoordinator;

    public GatewayResources(StateView stateView, EventDispatcher eventDispatcher,
                            ShardCoordinator shardCoordinator) {
        this.stateView = stateView;
        this.eventDispatcher = eventDispatcher;
        this.shardCoordinator = shardCoordinator;
    }

    /**
     * Repository aggregate view of all caching related operations. Discord Gateway mandates its clients to cache its
     * updates through events coming from the real-time websocket. The {@link StateView} is a read-only facade for
     * {@link StateHolder} which is the mediator for the underlying {@link Store} instances for each cached entity.
     *
     * @return the {@link StateView} tied to this {@link GatewayResources}
     */
    public StateView getStateView() {
        return stateView;
    }

    /**
     * Distributes events to subscribers. Starting from v3.1, the {@link EventDispatcher} is capable of distributing
     * events from all {@link GatewayClient} connections (shards) that were specified when this
     * {@link GatewayDiscordClient} was created.
     *
     * @return the {@link EventDispatcher} tied to this {@link GatewayResources}
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Returns the {@link ShardCoordinator} that is capable of coordinating certain shard actions.
     *
     * @return the {@link ShardCoordinator} tied to this {@link GatewayResources}
     */
    public ShardCoordinator getShardCoordinator() {
        return shardCoordinator;
    }
}
