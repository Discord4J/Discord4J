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

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

public class VoiceServerUpdateEvent extends Event {

    private final String token;
    private final long guildId;
    private final String endpoint;

    public VoiceServerUpdateEvent(DiscordClient client, String token, long guildId, String endpoint) {
        super(client);
        this.token = token;
        this.guildId = guildId;
        this.endpoint = endpoint;
    }

    public String getToken() {
        return token;
    }

    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    public String getEndpoint() {
        return endpoint;
    }
}
