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
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Dispatched in two different scenarios:
 * <ol>
 *     <li>The bot is kicked from or leaves a guild.</li>
 *     <li>A guild becomes unavailable during an outage. In this scenario, {@link #unavailable} will be true.</li>
 * </ol>
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-delete">Guild Delete</a>
 */
public class GuildDeleteEvent extends GuildEvent {

    private final long guildId;
    @Nullable
    private final Guild guild;
    private final boolean unavailable;

    public GuildDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId, @Nullable Guild guild, boolean unavailable) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.guild = guild;
        this.unavailable = unavailable;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} that is involved in the event.
     *
     * @return The ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Gets the {@link Guild} involved in this event, if present.
     *
     * @return The {@link Guild} involved in this event, if present.
     */
    public Optional<Guild> getGuild() {
        return Optional.ofNullable(guild);
    }

    /**
     * Gets whether or not the {@link Guild} is now unavailable.
     *
     * @return Whether or not the {@link Guild} is unavailable.
     */
    public boolean isUnavailable() {
        return unavailable;
    }

    @Override
    public String toString() {
        return "GuildDeleteEvent{" +
                "guildId=" + guildId +
                ", guild=" + guild +
                ", unavailable=" + unavailable +
                '}';
    }
}
