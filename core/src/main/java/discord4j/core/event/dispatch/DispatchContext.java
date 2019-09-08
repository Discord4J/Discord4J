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

import discord4j.core.Gateway;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.json.dispatch.Dispatch;

/**
 * Represents gateway dispatch data enriched with context for processing through a
 * {@link DispatchHandler} defined under {@link DispatchHandlers}
 *
 * @param <D> the type of the {@link discord4j.gateway.json.dispatch.Dispatch} payload
 */
public class DispatchContext<D extends Dispatch> {

    private final D dispatch;
    private final Gateway gateway;
    private final ShardInfo shardInfo;

    public static <D extends Dispatch> DispatchContext<D> of(D dispatch, Gateway gateway, ShardInfo shardInfo) {
        return new DispatchContext<>(dispatch, gateway, shardInfo);
    }

    private DispatchContext(D dispatch, Gateway gateway, ShardInfo shardInfo) {
        this.dispatch = dispatch;
        this.gateway = gateway;
        this.shardInfo = shardInfo;
    }

    public D getDispatch() {
        return dispatch;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public ShardInfo getShardInfo() {
        return shardInfo;
    }
}
