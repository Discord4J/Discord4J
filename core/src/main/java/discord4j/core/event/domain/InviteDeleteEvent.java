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
package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Dispatched when an invite to a channel is deleted.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#invite-delete">Invite Delete</a>
 */
public class InviteDeleteEvent extends Event {

    private final @Nullable Long guildId;
    private final long channelId;
    private final String code;

    public InviteDeleteEvent(GatewayDiscordClient client, ShardInfo shardInfo, @Nullable Long guildId, long channelId, String code) {
        super(client, shardInfo);
        this.guildId = guildId;
        this.channelId = channelId;
        this.code = code;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event, if present.
     *
     * @return The ID of the guild involved, if present.
     */
    public Optional<@Nullable Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Guild} that had an invite deleted in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Channel} where the invite was deleted.
     *
     * @return The ID of the channel involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Gets the invite code (unique ID).
     *
     * @return The invite code (unique ID).
     */
    public final String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "InviteDeleteEvent{" +
            "guildId=" + guildId +
            ", channelId=" + channelId +
            ", code=" + code +
            '}';
    }
}
