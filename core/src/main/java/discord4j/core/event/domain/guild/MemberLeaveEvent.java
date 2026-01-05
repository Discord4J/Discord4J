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
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Dispatched when a user leaves a guild <b>OR</b> is kicked from it.
 * <p>
 * Discord does not differentiate between a user leaving on their own and being kicked. Except through audit logs, it is
 * not possible to tell the difference between these.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-member-remove">Guild Member Remove</a>
 */
public class MemberLeaveEvent extends GuildEvent {

    private final User user;
    private final long guildId;
    @Nullable
    private final Member member;

    public MemberLeaveEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, User user, long guildId, @Nullable Member member) {
        super(gateway, shardInfo);
        this.user = user;
        this.guildId = guildId;
        this.member = member;
    }

    /**
     * Gets the {@link User} that has left the {@link Guild} in this event.
     *
     * @return The {@link User} that has left the {@link Guild}
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} that the {@link User} has left in this event.
     *
     * @return The ID of the {@link Guild}.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} that the {@link User} has left in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} that the {@link User} has left.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the {@link Member} object of the {@link User} that has left the {@link Guild} in this event, if present.
     *
     * @return The {@link Member} object of the {@link User} that has left the {@link Guild}, if present.
     */
    public Optional<Member> getMember() {
        return Optional.ofNullable(member);
    }

    @Override
    public String toString() {
        return "MemberLeaveEvent{" +
                "user=" + user +
                ", guildId=" + guildId +
                '}';
    }
}
