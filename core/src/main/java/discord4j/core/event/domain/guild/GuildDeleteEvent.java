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

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Dispatched in two different scenarios:
 * <ol>
 * <li>The bot is kicked from or leaves a guild.</li>
 * <li>A guild becomes unavailable during an outage. In this scenario, {@link #unavailable} will be true.</li>
 * </ol>
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-delete">Guild Delete</a>
 */
public class GuildDeleteEvent extends GuildEvent {

    private final long guildId;
    private final Guild guild;
    private final boolean unavailable;

    public GuildDeleteEvent(DiscordClient client, long guildId, @Nullable Guild guild, boolean unavailable) {
        super(client);
        this.guildId = guildId;
        this.guild = guild;
        this.unavailable = unavailable;
    }

    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    public Optional<Guild> getGuild() {
        return Optional.ofNullable(guild);
    }

    public boolean isUnavailable() {
        return unavailable;
    }
}
