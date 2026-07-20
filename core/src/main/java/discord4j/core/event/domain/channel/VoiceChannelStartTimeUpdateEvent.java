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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

/**
 * Dispatched when a {@link VoiceChannel} start time changes.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://docs.discord.com/developers/events/gateway-events#voice-channel-start-time-update">
 * <p>
 * Voice Channel Start Time Update</a>
 */
public class VoiceChannelStartTimeUpdateEvent extends ChannelEvent {

    private final long guildId;
    private final long channelId;
    private final @Nullable Instant voiceStartTime;

    public VoiceChannelStartTimeUpdateEvent(
            final GatewayDiscordClient gateway,
            final ShardInfo shardInfo,
            final long guildId,
            final long channelId,
            final @Nullable Instant voiceStartTime
    ) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.channelId = channelId;
        this.voiceStartTime = voiceStartTime;
    }

    /**
     * Gets the {@link Snowflake} ID of the guild that had a voice channel start time update in this event.
     *
     * @return The ID of the guild involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(this.guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} that had a voice channel start time update in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     */
    public Mono<Guild> getGuild() {
        return this.getClient().getGuildById(this.getGuildId());
    }

    /**
     * Gets the {@link Snowflake} ID of the channel that had a voice channel start time update in this event.
     *
     * @return The ID of the channel involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(this.channelId);
    }

    /**
     * Requests to retrieve the {@link VoiceChannel}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link VoiceChannel} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> getChannel() {
        return this.getClient().getChannelById(this.getChannelId()).cast(VoiceChannel.class);
    }

    /**
     * Gets the new voice start time of the channel, if present.
     *
     * @return The new voice start time of the channel, if present.
     */
    public Optional<Instant> getVoiceStartTime() {
        return Optional.ofNullable(this.voiceStartTime);
    }

    @Override
    public String toString() {
        return "VoiceChannelStartTimeUpdateEvent{" +
                "guildId=" + this.guildId +
                ", channelId=" + this.channelId +
                ", voiceStartTime=" + this.voiceStartTime +
                '}';
    }
}
