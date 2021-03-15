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
 * Dispatched when a user is unbanned from a guild.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-ban-remove">Guild Ban Remove</a>
 */
public class UnbanEvent extends GuildEvent {

    private final User user;
    private final long guildId;

    public UnbanEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, User user, long guildId) {
        super(gateway, shardInfo);
        this.user = user;
        this.guildId = guildId;
    }

    /**
     * Gets the {@link User} that has been unbanned in this event.
     *
     * @return The {@link User} that has been unbanned.
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} the {@link User} was unbanned from.
     *
     * @return The ID of the Guild involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the Guild the Member was unbanned from.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} the {@link User} was
     * unbanned from. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public String toString() {
        return "UnbanEvent{" +
                "user=" + user +
                ", guildId=" + guildId +
                '}';
    }
}
