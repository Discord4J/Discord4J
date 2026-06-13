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
import discord4j.core.object.entity.channel.Channel;
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
    private final @Nullable Long guildId;
    private final @Nullable Member member;
    private final Channel.@Nullable Type channelType;

    public MessageCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Message message, @Nullable Long guildId, @Nullable Member member, Channel.@Nullable Type channelType) {
        super(gateway, shardInfo);
        this.message = message;
        this.guildId = guildId;
        this.member = member;
        this.channelType = channelType;
    }

    /**
     * Gets the {@link Message} that was created in this event.
     *
     * @return The {@link Message} that was created.
     */
    public Message getMessage() {
        return this.message;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} the {@link Message} was created in, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return The ID of the {@link Guild} containing the {@link Message}, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(this.guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Guild} the {@link Message} was created in, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} the message was created in,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(this.getGuildId()).flatMap(this.getClient()::getGuildById);
    }

    /**
     * Gets the {@link Member} who has sent the {@link Message} created in this event, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return The {@link Member} who has sent the {@link Message} created in this event, if present.
     */
    public Optional<Member> getMember() {
        return Optional.ofNullable(this.member);
    }

    /**
     * Retrieves the {@link Channel.Type} in which the {@link Message} was created, if present.
     *
     * @return The {@link Channel.Type type of channel} the message was sent in, if present.
     */
    public Optional<Channel.Type> getChannelType() {
        return Optional.ofNullable(this.channelType);
    }

    @Override
    public String toString() {
        return "MessageCreateEvent{" +
                "message=" + this.message +
                ", guildId=" + this.guildId +
                ", member=" + this.member +
                ", channelType=" + this.channelType +
                '}';
    }
}
