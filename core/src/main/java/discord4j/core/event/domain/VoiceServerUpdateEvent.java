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
package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

/**
 * Dispatched when the current user is initially connecting to a voice channel, and when the current voice instance
 * fails over to a new server (guild's voice server is updated).
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#voice-server-update">Voice Server Update</a>
 */
public class VoiceServerUpdateEvent extends Event {

    private final String token;
    private final long guildId;
    @Nullable
    private final String endpoint;

    public VoiceServerUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, String token, long guildId, @Nullable String endpoint) {
        super(gateway, shardInfo);
        this.token = token;
        this.guildId = guildId;
        this.endpoint = endpoint;
    }

    /**
     * Gets the voice connection token for the guild.
     *
     * @return The voice connection token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the {@link Snowflake} ID of the guild whose voice server has been updated in this event.
     *
     * @return The ID of the guild involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} whose voice server has been updated in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} whose voice server has been
     * updated.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the voice server host's endpoint URL.
     *
     * @return The void server host's endpoint URL.
     */
    @Nullable
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        return "VoiceServerUpdateEvent{" +
                "token='" + token + '\'' +
                ", guildId=" + guildId +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
