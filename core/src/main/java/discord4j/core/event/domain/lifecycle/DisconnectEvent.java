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

import discord4j.common.close.CloseStatus;
import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Indicates that a gateway connection is disconnected.
 * <p>
 * This event is dispatched by Discord4J.
 */
public class DisconnectEvent extends GatewayLifecycleEvent {
    private final CloseStatus status;
    @Nullable
    private final Throwable cause;

    public DisconnectEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, CloseStatus status, @Nullable Throwable cause) {
        super(gateway, shardInfo);
        this.status = status;
        this.cause = cause;
    }

    public CloseStatus getStatus() {
        return status;
    }

    public Optional<Throwable> getCause() {
        return Optional.ofNullable(cause);
    }

    @Override
    public String toString() {
        return "Gateway connection terminated";
    }
}
