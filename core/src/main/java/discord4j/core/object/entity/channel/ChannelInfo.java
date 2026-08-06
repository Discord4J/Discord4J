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
package discord4j.core.object.entity.channel;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ChannelInfoData;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

/**
 * Discord channel info.
 *
 * @see <a href="https://docs.discord.com/developers/events/gateway-events#channel-info-channel-info-channel-structure">Channel Info</a>
 */
public class ChannelInfo implements Entity {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ChannelInfoData data;

    private final long guildId;

    public ChannelInfo(final GatewayDiscordClient gateway, long guildId, final ChannelInfoData data) {
        this.gateway = gateway;
        this.guildId = guildId;
        this.data = data;
    }

    public ChannelInfoData getData() {
        return this.data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(this.getData().id());
    }

    /**
     * Requests to retrieve the {@link Channel}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Channel} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Channel> getChannel() {
        return this.getClient().getChannelById(this.getId());
    }

    /**
     * Gets the guild id.
     *
     * @return the guild id.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(this.guildId);
    }

    /**
     * Requests to retrieve the {@link Guild}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     */
    public Mono<Guild> getGuild() {
        return this.getClient().getGuildById(this.getGuildId());
    }

    /**
     * Gets the channel's status, if present.
     *
     * @return The channel's status, if present.
     */
    public Optional<String> getStatus() {
        return Possible.flatOpt(this.getData().status());
    }

    /**
     * Gets the time at which the user started speaking in this voice channel, if present.
     *
     * @return The time at which the user started speaking in this voice channel, if present.
     */
    public Optional<Instant> getVoiceStartTime() {
        return Possible.flatOpt(this.getData().voiceStartTime()).map(Instant::ofEpochSecond);
    }

    @Override
    public String toString() {
        return "ChannelInfo{" +
                "data=" + data +
                '}';
    }
}
