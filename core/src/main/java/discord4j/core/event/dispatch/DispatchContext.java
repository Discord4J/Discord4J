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

package discord4j.core.event.dispatch;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.state.StateHolder;
import discord4j.gateway.ShardInfo;

/**
 * Represents gateway dispatch data enriched with context for processing through a
 * {@link DispatchHandler} defined under {@link DispatchHandlers}.
 *
 * @param <D> The type of the payload.
 */
public class DispatchContext<D> {

    private final D dispatch;
    private final GatewayDiscordClient gateway;
    private final StateHolder stateHolder;
    private final ShardInfo shardInfo;

    public static <D> DispatchContext<D> of(D dispatch, GatewayDiscordClient gateway,
                                            StateHolder stateHolder, ShardInfo shardInfo) {
        return new DispatchContext<>(dispatch, gateway, stateHolder, shardInfo);
    }

    private DispatchContext(D dispatch, GatewayDiscordClient gateway, StateHolder stateHolder, ShardInfo shardInfo) {
        this.dispatch = dispatch;
        this.gateway = gateway;
        this.stateHolder = stateHolder;
        this.shardInfo = shardInfo;
    }

    public D getDispatch() {
        return dispatch;
    }

    public GatewayDiscordClient getGateway() {
        return gateway;
    }

    public StateHolder getStateHolder() {
        return stateHolder;
    }

    public ShardInfo getShardInfo() {
        return shardInfo;
    }
}
