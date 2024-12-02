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
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.ForumChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a {@link ForumChannel} is updated in a guild.
 * <p>
 * The old news channel may not be present if news channels are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#channel-update">Channel Update</a>
 */
public class ForumChannelUpdateEvent extends ChannelEvent {

    private final ForumChannel current;
    private final ForumChannel old;

    public ForumChannelUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, ForumChannel current, @Nullable ForumChannel old) {
        super(gateway, shardInfo);
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the current, new version of the {@link ForumChannel} that was updated in this event.
     *
     * @return The current version of the updated {@link ForumChannel}.
     */
    public ForumChannel getCurrent() {
        return current;
    }

    /**
     * Gets the old version of the {@link ForumChannel} that was updated in this event, if present.
     *
     * @return The old version of the updated {@link ForumChannel}, if present.
     */
    public Optional<ForumChannel> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "ForumChannelUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
