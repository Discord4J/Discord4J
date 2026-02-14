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
package discord4j.core.event.domain.message;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Dispatched when a message is sent in a message channel.
 * <p>
 * {@link #guildId} and {@link #member} may not be present if the message was sent in a private channel.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#message-create">Message Create</a>
 */
public class MessageCreateEvent extends MessageEvent {

    private final Message message;
    @Nullable
    private final Long guildId;
    @Nullable
    private final Member member;

    public MessageCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Message message, @Nullable Long guildId, @Nullable Member member) {
        super(gateway, shardInfo);
        this.message = message;
        this.guildId = guildId;
        this.member = member;
    }

    /**
     * Gets the {@link Message} that was created in this event.
     *
     * @return The {@link Message} that was created.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} the {@link Message} was created in, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return The ID of the {@link Guild} containing the {@link Message}, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Guild} the {@link Message} was created in, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} the message was created in,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * Gets the {@link Member} who has sent the {@link Message} created in this event, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return The {@link Member} who has sent the {@link Message} created in this event, if present.
     */
    public Optional<Member> getMember() {
        return Optional.ofNullable(member);
    }

    @Override
    public String toString() {
        return "MessageCreateEvent{" +
                "message=" + message +
                ", guildId=" + guildId +
                ", member=" + member +
                '}';
    }
}
