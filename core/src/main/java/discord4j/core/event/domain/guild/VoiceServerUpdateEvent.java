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
package discord4j.core.event.domain.guild;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * Dispatched when initially connecting to a voice channel.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#voice-server-update">Voice Server Update</a>
 */
public class VoiceServerUpdateEvent extends AbstractGuildEvent {

    private final String token;
    @Nullable
    private final String endpoint;

    public VoiceServerUpdateEvent(DiscordClient client, String token, long guildId, @Nullable String endpoint) {
        super(client, guildId);
        this.token = token;
        this.endpoint = endpoint;
    }

    public String getToken() {
        return token;
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Nullable
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        return "VoiceServerUpdateEvent{" +
                "token='" + token + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
