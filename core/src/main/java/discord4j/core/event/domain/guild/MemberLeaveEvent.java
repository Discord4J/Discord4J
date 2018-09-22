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
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a user leaves a guild <b>OR</b> is kicked from it.
 * <p>
 * Discord does not differentiate between a user leaving on their own and being kicked. Except through audit logs, it is
 * not possible to tell the difference between these.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-member-remove">Guild Member Remove</a>
 */
public class MemberLeaveEvent extends GuildEvent {

    private final User user;
    private final long guildId;

    public MemberLeaveEvent(DiscordClient client, User user, long guildId) {
        super(client);
        this.user = user;
        this.guildId = guildId;
    }

    public User getUser() {
        return user;
    }

    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public String toString() {
        return "MemberLeaveEvent{" +
                "user=" + user +
                ", guildId=" + guildId +
                '}';
    }
}
