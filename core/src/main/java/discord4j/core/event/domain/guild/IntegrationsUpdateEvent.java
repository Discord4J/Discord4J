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
package discord4j.core.event.domain.guild;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Dispatched when guild integrations are updated.
 * <p>
 * This event is dispatched by Discord
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-integrations-update">Guild Integrations
 * Update</a>
 */
public class IntegrationsUpdateEvent extends GuildEvent {

    private final long guildId;

    public IntegrationsUpdateEvent(DiscordClient client, long guildId) {
        super(client);
        this.guildId = guildId;
    }

    /**
     * The Snowflake ID of the Guild involved in this event.
     *
     * @return The Snowflake ID of the guild.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the Guild whose integrations have been updated.
     *
     * @return A {@link Mono} where, upon successful completion, emits the Guild involved in the event. If an error is received, it is emitted through the Mono.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public String toString() {
        return "IntegrationsUpdateEvent{" +
                "guildId=" + guildId +
                '}';
    }
}
