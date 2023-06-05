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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.event.domain.guild;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.ScheduledEvent;
import discord4j.core.object.entity.User;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a user has unsubscribed from a guild scheduled event.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#guild-scheduled-event-user-remove">Discord</a>
 */
public class ScheduledEventUserRemoveEvent extends GuildEvent {

    private final long guildId;
    private final long scheduledEventId;
    private final long userId;

    public ScheduledEventUserRemoveEvent(GatewayDiscordClient gateway, ShardInfo shardInfo,
                                         long guildId, long scheduledEventId, long userId) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.scheduledEventId = scheduledEventId;
        this.userId = userId;
    }

    /**
     * Return the guild ID of the scheduled event.
     *
     * @return a guild snowflake
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Return the guild of the scheduled event.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Return the scheduled event ID.
     *
     * @return a scheduled event snowflake
     */
    public Snowflake getScheduledEventId() {
        return Snowflake.of(scheduledEventId);
    }

    /**
     * Return the scheduled event.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link ScheduledEvent} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ScheduledEvent> getScheduledEvent() {
        return getClient().getScheduledEventById(getGuildId(), getScheduledEventId());
    }

    /**
     * Return the unsubscribing user ID.
     *
     * @return a user snowflake
     */
    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    /**
     * Return the unsubscribing user.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link User} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    /**
     * Return the unsubscribing guild Member.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link Member} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMember() {
        return getClient().getMemberById(getGuildId(), getUserId());
    }

    @Override
    public String toString() {
        return "ScheduledEventUserRemoveEvent{" +
                "guildId=" + guildId +
                ", scheduledEventId=" + scheduledEventId +
                ", userId=" + userId +
                "}";
    }
}
