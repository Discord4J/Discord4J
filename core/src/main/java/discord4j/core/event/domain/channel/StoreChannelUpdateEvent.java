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
package discord4j.core.event.domain.channel;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.StoreChannel;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Dispatched when a {@link StoreChannel} is updated in a guild.
 * <p>
 * The old store channel may not be present if channels are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#channel-update">Channel Update</a>
 */
public class StoreChannelUpdateEvent extends ChannelEvent {

    private final StoreChannel current;
    private final StoreChannel old;

    public StoreChannelUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, StoreChannel current, @Nullable StoreChannel old) {
        super(gateway, shardInfo);
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the current, new version of the {@link StoreChannel} that was updated in this event.
     *
     * @return The current version of the updated {@link StoreChannel}.
     */
    public StoreChannel getCurrent() {
        return current;
    }

    /**
     * Gets the old version of the {@link StoreChannel} that was updated in this event, if present.
     * This may not be available if {@code StoreChannels} are not stored.
     *
     * @return The old version of the updated {@link StoreChannel}, if present.
     */
    public Optional<StoreChannel> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "StoreChannelUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
