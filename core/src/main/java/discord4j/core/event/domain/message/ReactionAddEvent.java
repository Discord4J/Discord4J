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
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.emoji.Emoji;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import discord4j.rest.util.Color;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Dispatched when a reaction is added to a message.
 * <p>
 * {@link #getGuildId()} may not be present if the message was in a private channel.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/events/gateway-events#message-reaction-add">Message Reaction Add</a>
 */
public class ReactionAddEvent extends ReactionUserEmojiEvent {

    @Nullable
    private final Member member;
    private final long messageAuthorId;
    private final List<String> burstColors;

    public ReactionAddEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long userId, long channelId,
                            long messageId, @Nullable Long guildId,
                            Emoji emoji, @Nullable Member member, long messageAuthorId, boolean burst,
                            List<String> burstColors, int type) {
        super(gateway, shardInfo, channelId, messageId, guildId, emoji, userId, burst, type);
        this.member = member;
        this.messageAuthorId = messageAuthorId;
        this.burstColors = burstColors;
    }

    /**
     * Gets the member who reacted, if present.
     * This may not be available if the reaction is to a {@code Message} in a private channel.
     *
     * @return The member who reacted, if present.
     */
    public Optional<Member> getMember() {
        return Optional.ofNullable(member);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link User} who sent the {@link Message} that was reacted to.
     * Note that this id will be 0 if the message was sent by a webhook.
     *
     * @return The ID of the {@link User} who sent the {@link Message} that was reacted to.
     */
    public Snowflake getMessageAuthorId() {
        return Snowflake.of(messageAuthorId);
    }

    /**
     * Get a list of HEX colors used for super reaction.
     *
     * @return A list of {@link Color} used in this reaction.
     */
    public List<Color> getSuperColors() {
        return this.burstColors.stream().map(Color::of).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ReactionAddEvent{" +
            ", member=" + member +
            ", burstColors=" + burstColors +
            "} " + super.toString();
    }
}
