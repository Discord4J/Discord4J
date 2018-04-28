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
package discord4j.core.object;

import discord4j.common.json.response.UserResponse;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.bean.ReactionBean;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.bean.UserBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.PaginationUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A Discord message reaction.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#reaction-object">Reaction Object</a>
 */
public final class Reaction implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final ReactionBean data;

    /** The ID of the channel this reaction is associated to. */
    private final long channelId;

    /** The ID of the message this reaction is associated to. */
    private final long messageId;

    /**
     * Constructs a {@code Reaction} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param channelId The ID of the channel this reaction is associated to.
     * @param messageId The ID of the message this reaction is associated to.
     */
    public Reaction(final ServiceMediator serviceMediator, final ReactionBean data, final long channelId,
                    final long messageId) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
        this.channelId = channelId;
        this.messageId = messageId;
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the number times this emoji has been used to react.
     *
     * @return The number of times this emoji has been used to react.
     */
    public int getCount() {
        return data.getCount();
    }

    /**
     * Gets whether the current user reacted using this emoji.
     *
     * @return {@code true} if the current user reacted using this emoji, {@code false} otherwise.
     */
    public boolean hasReacted() {
        return data.isMe();
    }

    /**
     * Gets the ID of the emoji for this reaction, if present.
     *
     * @return The ID of the emoji for this reaction, if present.
     */
    public Optional<Snowflake> getEmojiId() {
        return Optional.ofNullable(data.getEmojiId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the emoji for this reaction, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji emoji} for this reaction,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> getGuildEmoji() {
        return Mono.justOrEmpty(getEmojiId())
                .flatMap(emojiId -> getGuild()
                        .map(Guild::getId)
                        .flatMap(guildId -> getClient().getGuildEmojiById(guildId, emojiId)));
    }

    /**
     * Gets the name of the emoji for this reaction.
     *
     * @return The name of the emoji for this reaction.
     */
    public String getEmojiName() {
        return data.getEmojiName();
    }

    /**
     * Gets the ID of the message this reaction is associated to.
     *
     * @return The ID of the message this reaction is associated to.
     */
    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    /**
     * Requests to retrieve the message this reaction is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message message} this reaction is
     * associated to. If an error is received it is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessage() {
        return getClient().getMessageById(getChannelId(), getMessageId());
    }

    /**
     * Gets the ID of the channel this reaction is associated to.
     *
     * @return The ID of the channel this reaction is associated to.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the channel this reaction is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel channel} this reaction
     * is associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getMessageChannelById(getChannelId());
    }

    /**
     * Requests to retrieve the guild this reaction is associated to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this reaction is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getChannel().ofType(GuildChannel.class).flatMap(GuildChannel::getGuild);
    }

    /**
     * Requests to retrieve the users that reacted with this emoji.
     *
     * @return A {@link Flux} that continually emits the {@link User users} that reacted with this emoji. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<User> getReactors() {
        Function<Map<String, Object>, Flux<UserResponse>> makeRequest = params ->
                serviceMediator.getRestClient().getChannelService()
                        .getReactions(getChannelId().asLong(), messageId, getEmojiName(), params);

        return PaginationUtil.paginateAfter(makeRequest, UserResponse::getId, 0L, 100)
                .map(UserBean::new)
                .map(bean -> new User(serviceMediator, bean));
    }

    /**
     * Requests to delete the reaction by the specified user.
     *
     * @param userId The ID of the specified user to delete the reaction by.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction by the specified
     * user has been deleted. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(final Snowflake userId) {
        return serviceMediator.getRestClient().getChannelService()
                .deleteReaction(channelId, messageId, getEmojiName(), userId.asLong());
    }

    /**
     * Requests to delete the reaction by the specified user.
     *
     * @param user The specified user to delete the reaction by.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction by the specified
     * user has been deleted. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(final User user) {
        return delete(user.getId());
    }
}
