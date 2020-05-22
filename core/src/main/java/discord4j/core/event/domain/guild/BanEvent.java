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
import discord4j.core.object.entity.User;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a user is banned from a guild.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-ban-add">Guild Ban Add</a>
 */
public class BanEvent extends GuildEvent {

    private final User user;
    private final long guildId;

    public BanEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, User user, long guildId) {
        super(gateway, shardInfo);
        this.user = user;
        this.guildId = guildId;
    }

    /**
     * Gets the {@link User} that has been banned from the {@link Guild}.
     *
     * @return The {@link User} that has been banned.
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} in this event.
     *
     * @return The ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} the {@link User} was banned from.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in this event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public String toString() {
        return "BanEvent{" +
                "user=" + user +
                ", guildId=" + guildId +
                '}';
    }
}
