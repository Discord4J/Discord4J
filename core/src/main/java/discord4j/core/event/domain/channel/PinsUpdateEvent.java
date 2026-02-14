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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

/**
 * Dispatched when a message is pinned or unpinned in a message channel.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#channel-pins-update">Channel Pins Update</a>
 */
public class PinsUpdateEvent extends ChannelEvent {

    private final long channelId;
    @Nullable
    private final Long guildId;
    @Nullable
    private final Instant lastPinTimestamp;

    public PinsUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long channelId, @Nullable Long guildId, @Nullable Instant lastPinTimestamp) {
        super(gateway, shardInfo);
        this.channelId = channelId;
        this.guildId = guildId;
        this.lastPinTimestamp = lastPinTimestamp;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link MessageChannel} the pinned/unpinned
     * {@link discord4j.core.object.entity.Message} is in.
     *
     * @return the ID of the {@link MessageChannel} involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the {@link MessageChannel} the pinned/unpinned
     * {@link discord4j.core.object.entity.Message} is in.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} the pinned/unpinned
     * {@link discord4j.core.object.entity.Message} is in, if this happened in a guild.
     * This may not be available if the {@code Message} is in a private channel.
     *
     * @return The ID of the {@link Guild} involved, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Guild} the pinned/unpinned
     * {@link discord4j.core.object.entity.Message} is in, if this happened in a guild.
     * This may not be available if the {@code Message} is in a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} containing the
     * {@link Message} involved, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * Gets the ISO8601 timestamp of when the last pinned {@link discord4j.core.object.entity.Message} w
     * as pinned, if present. This is NOT the timestamp of when the {@code Message} was created.
     *
     * @return The timestamp of the when the last pinned {@link discord4j.core.object.entity.Message} was pinned,
     * if present.
     */
    public Optional<Instant> getLastPinTimestamp() {
        return Optional.ofNullable(lastPinTimestamp);
    }

    @Override
    public String toString() {
        return "PinsUpdateEvent{" +
                "channelId=" + channelId +
                ", lastPinTimestamp=" + lastPinTimestamp +
                '}';
    }
}
