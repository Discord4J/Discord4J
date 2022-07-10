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
import discord4j.gateway.ShardInfo;
import discord4j.gateway.state.StatefulDispatch;

import java.util.Optional;

/**
 * Represents gateway dispatch data enriched with context for processing through a
 * {@link DispatchHandler} defined under {@link DispatchHandlers}.
 *
 * @param <D> the type of the payload
 * @param <S> the type of the old state
 */
public class DispatchContext<D, S> {

    private final StatefulDispatch<D, S> statefulDispatch;
    private final GatewayDiscordClient gateway;

    public static <D, S> DispatchContext<D, S> of(StatefulDispatch<D, S> statefulDispatch,
                                                  GatewayDiscordClient gateway) {
        return new DispatchContext<>(statefulDispatch, gateway);
    }

    private DispatchContext(StatefulDispatch<D, S> statefulDispatch, GatewayDiscordClient gateway) {
        this.statefulDispatch = statefulDispatch;
        this.gateway = gateway;
    }

    public GatewayDiscordClient getGateway() {
        return gateway;
    }

    public D getDispatch() {
        return statefulDispatch.getDispatch();
    }

    public ShardInfo getShardInfo() {
        return statefulDispatch.getShardInfo();
    }

    public Optional<S> getOldState() {
        return statefulDispatch.getOldState();
    }
}
