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

/**
 * Dispatched in three different scenarios:
 * <ol>
 * <li>After the bot connects to Discord (after {@link discord4j.core.event.domain.lifecycle.ReadyEvent ReadyEvent}
 * is dispatched), this event will be dispatched for all guilds the bot is in on this shard.</li>
 * <li>After an outage (in which many {@link discord4j.core.event.domain.guild.GuildDeleteEvent guild deletes} will
 * be dispatched), this event will be dispatched as guilds become available again.</li>
 * <li>When the bot is added to a guild.</li>
 * </ol>
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-create">Guild Create</a>
 */
public class GuildCreateEvent extends GuildEvent {

    private final Guild guild;

    public GuildCreateEvent(DiscordClient client, Guild guild) {
        super(client);
        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }
}
