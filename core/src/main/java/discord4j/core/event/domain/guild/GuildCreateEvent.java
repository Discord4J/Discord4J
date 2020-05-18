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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.gateway.ShardInfo;

/**
 * Dispatched in three different scenarios:
 * <ol>
 *     <li>After the bot connects to Discord (after {@link discord4j.core.event.domain.lifecycle.ReadyEvent ReadyEvent}
 *     is dispatched), this event will be dispatched for all guilds the bot is in on this shard.</li>
 *     <li>After an outage (in which many {@link discord4j.core.event.domain.guild.GuildDeleteEvent guild deletes} will
 *     be dispatched), this event will be dispatched as guilds become available again.</li>
 *     <li>When the bot is added to a guild.</li>
 * </ol>
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-create">Guild Create</a>
 */
public class GuildCreateEvent extends GuildEvent {

    private final Guild guild;

    public GuildCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Guild guild) {
        super(gateway, shardInfo);
        this.guild = guild;
    }

    /**
     * Gets the {@link Guild} that has become available in this event.
     *
     * @return The {@link Guild} that has become available.
     */
    public Guild getGuild() {
        return guild;
    }

    @Override
    public String toString() {
        return "GuildCreateEvent{" +
                "guild=" + guild +
                '}';
    }
}
