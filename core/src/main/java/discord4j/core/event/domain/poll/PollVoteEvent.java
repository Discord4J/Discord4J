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
package discord4j.core.event.domain.poll;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.poll.Poll;
import discord4j.core.object.entity.poll.PollAnswer;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Dispatched when a vote is added or removed from a poll.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#polls">Polls events</a>
 */
public class PollVoteEvent extends Event {

    private final Snowflake userId;
    private final Snowflake channelId;
    private final Snowflake messageId;
    private final Optional<Snowflake> guildId;
    private final int answerId;

    protected PollVoteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Snowflake userId, Snowflake channelId, Snowflake messageId, Optional<Snowflake> guildId, int answerId) {
        super(gateway, shardInfo);

        this.userId = userId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.guildId = guildId;
        this.answerId = answerId;
    }

    /**
     * Get the {@link Snowflake} ID of the user who voted in this event.
     *
     * @return The ID of the user who voted.
     */
    public Snowflake getUserId() {
        return this.userId;
    }

    /**
     * Get the {@link User} who voted in this event.
     *
     * @return The user who voted.
     */
    public Mono<User> getUser() {
        return super.getClient().getUserById(this.userId);
    }

    /**
     * Get the {@link Snowflake} ID of the channel where the poll is.
     *
     * @return The ID of the channel where the poll is.
     */
    public Snowflake getChannelId() {
        return this.channelId;
    }

    /**
     * Get the {@link MessageChannel} where the poll is.
     *
     * @return The channel where the poll is.
     */
    public Mono<MessageChannel> getChannel() {
        return super.getClient().getChannelById(this.channelId).ofType(MessageChannel.class);
    }

    /**
     * Get the {@link Snowflake} ID of the message where the poll is.
     *
     * @return The ID of the message where the poll is.
     */
    public Snowflake getMessageId() {
        return this.messageId;
    }

    /**
     * Get the {@link Message} where the poll is.
     *
     * @return The message where the poll is.
     */
    public Mono<Message> getMessage() {
        return super.getClient().getMessageById(this.channelId, this.messageId);
    }

    /**
     * Get the {@link Poll} where the vote was added.
     *
     * @return The poll where the vote was added.
     */
    public Mono<Poll> getPoll() {
        return super.getClient().getMessageById(this.channelId, this.messageId)
            .map(Message::getPoll)
            .flatMap(Mono::justOrEmpty);
    }

    /**
     * Get the {@link Snowflake} ID of the guild where the poll is.
     *
     * @return An {@link Optional} containing the ID of the guild where the poll is, or {@link Optional#empty()} if the
     * poll is in a DM.
     */
    public Optional<Snowflake> getGuildId() {
        return this.guildId;
    }

    /**
     * Get the ID of the answer that was voted.
     *
     * @return The ID of the answer that was voted.
     */
    public int getAnswerId() {
        return this.answerId;
    }

    /**
     * Get the {@link PollAnswer} that was voted.
     *
     * @return The answer that was voted.
     */
    public Mono<PollAnswer> getAnswer() {
        return getPoll()
            .flatMap(poll -> Mono.justOrEmpty(poll.getAnswerById(this.answerId)));
    }

}
