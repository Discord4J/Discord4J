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
package discord4j.core.object.entity;

import discord4j.common.annotations.Experimental;
import discord4j.common.json.UserResponse;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.Embed;
import discord4j.core.object.MessageReference;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.data.stored.ReactionBean;
import discord4j.core.object.data.stored.UserBean;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.PaginationUtil;
import discord4j.rest.json.request.SuppressEmbedsRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Discord message.
 * <p>
 * <a href="https://discordapp.com/developers/docs/resources/channel#message-object">Message Object</a>
 */
public final class Message implements Entity {

    /** The maximum amount of characters that can be in the contents of a message. */
    public static final int MAX_CONTENT_LENGTH = 2000;

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final MessageBean data;

    /**
     * Constructs a {@code Message} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Message(final ServiceMediator serviceMediator, final MessageBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    /**
     * Gets the ID of the channel the message was sent in.
     *
     * @return The ID of the channel the message was sent in.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(data.getChannelId());
    }

    /**
     * Requests to retrieve the channel the message was sent in.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel channel} the message
     * was sent in. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Gets the ID the webhook that generated this message, if present.
     *
     * @return The ID of the webhook that generated this message, if present.
     */
    public Optional<Snowflake> getWebhookId() {
        return Optional.ofNullable(data.getWebhookId()).map(Snowflake::of);
    }

    /**
     * Gets the author of this message, if present.
     *
     * @return The author of this message, if present.
     */
    public Optional<User> getAuthor() {
        return Optional.ofNullable(data.getAuthor()).map(bean -> new User(serviceMediator, bean));
    }

    /**
     * Requests to retrieve the author of this message as a {@link Member member} of the guild in which it was sent.
     *
     * @return A {@link Mono} where, upon successful completion, emits the author of this message as a
     * {@link Member member} of the guild in which it was sent, if present. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Member> getAuthorAsMember() {
        return Mono.justOrEmpty(getAuthor())
                .flatMap(author -> getGuild()
                        .map(Guild::getId)
                        .flatMap(author::asMember));
    }

    /**
     * Gets the contents of the message, if present.
     *
     * @return The contents of the message, if present.
     */
    public Optional<String> getContent() {
        // Even though the bean / responses say it's not nullable Discord is being stupid atm
        return Optional.ofNullable(data.getContent()).filter(content -> !content.isEmpty());
    }

    /**
     * Gets when this message was sent.
     *
     * @return When this message was sent.
     */
    public Instant getTimestamp() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.getTimestamp(), Instant::from);
    }

    /**
     * Gets when this message was edited, if present.
     *
     * @return When this message was edited, if present.
     */
    public Optional<Instant> getEditedTimestamp() {
        return Optional.ofNullable(data.getEditedTimestamp())
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets whether this was a TTS (Text-To-Speech) message.
     *
     * @return {@code true} if this message was a TTS (Text-To-Speech) message, {@code false} otherwise.
     */
    public boolean isTts() {
        return data.isTts();
    }

    /**
     * Gets whether this message mentions everyone.
     *
     * @return {@code true} if this message mentions everyone, {@code false} otherwise.
     */
    public boolean mentionsEveryone() {
        return data.isMentionEveryone();
    }

    /**
     * Gets the IDs of the users specifically mentioned in this message.
     *
     * @return The IDs of the users specifically mentioned in this message.
     */
    public Set<Snowflake> getUserMentionIds() {
        return Arrays.stream(data.getMentions())
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the users specifically mentioned in this message.
     *
     * @return A {@link Flux} that continually emits {@link User users} specifically mentioned in this message. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<User> getUserMentions() {
        return Flux.fromIterable(getUserMentionIds()).flatMap(getClient()::getUserById);
    }

    /**
     * Gets the IDs of the roles specifically mentioned in this message.
     *
     * @return The IDs of the roles specifically mentioned in this message.
     */
    public Set<Snowflake> getRoleMentionIds() {
        return Arrays.stream(data.getMentionRoles())
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the roles specifically mentioned in this message.
     *
     * @return A {@link Flux} that continually emits {@link Role roles} specifically mentioned in this message. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<Role> getRoleMentions() {
        return Flux.fromIterable(getRoleMentionIds())
                .flatMap(roleId -> getGuild()
                        .map(Guild::getId)
                        .flatMap(guildId -> getClient().getRoleById(guildId, roleId)));
    }

    /**
     * Gets any attached files.
     *
     * @return Any attached files.
     */
    public Set<Attachment> getAttachments() {
        return Arrays.stream(data.getAttachments())
                .map(bean -> new Attachment(serviceMediator, bean))
                .collect(Collectors.toSet());
    }

    /**
     * Gets any embedded content.
     *
     * @return Any embedded content.
     */
    public List<Embed> getEmbeds() {
        return Arrays.stream(data.getEmbeds())
                .map(bean -> new Embed(serviceMediator, bean))
                .collect(Collectors.toList());
    }

    /**
     * Gets the reactions to this message.
     *
     * @return The reactions to this message.
     */
    public Set<Reaction> getReactions() {
        final ReactionBean[] reactions = data.getReactions();
        return (reactions == null) ? Collections.emptySet() : Arrays.stream(reactions)
                .map(bean -> new Reaction(serviceMediator, bean))
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve the reactors (users) for the specified emoji for this message.
     *
     * @param emoji The emoji to get the reactors (users) for this message.
     * @return A {@link Flux} that continually emits the {@link User reactors} for the specified emoji for this message.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<User> getReactors(final ReactionEmoji emoji) {
        final Function<Map<String, Object>, Flux<UserResponse>> makeRequest = params ->
                serviceMediator.getRestClient().getChannelService()
                        .getReactions(getChannelId().asLong(), getId().asLong(),
                                EntityUtil.getEmojiString(emoji),
                                params)
                        .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));

        return PaginationUtil.paginateAfter(makeRequest, UserResponse::getId, 0L, 100)
                .map(UserBean::new)
                .map(bean -> new User(serviceMediator, bean));
    }

    /**
     * Gets whether this message is pinned.
     *
     * @return {@code true} if this message is pinned, {@code false} otherwise.
     */
    public boolean isPinned() {
        return data.isPinned();
    }

    /**
     * Requests to retrieve the webhook that generated this message, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook webhook} that generated this
     * message, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> getWebhook() {
        return Mono.justOrEmpty(getWebhookId()).flatMap(getClient()::getWebhookById);
    }

    /**
     * Returns the {@link MessageReference} in this message (Server Following feature), if present.
     *
     * @return The {@code MessageReference}, if present.
     */
    public Optional<MessageReference> getMessageReference() {
        return Optional.ofNullable(data.getMessageReference())
                .map(bean -> new MessageReference(serviceMediator, bean));
    }

    /**
     * Returns the flags of this {@link Message}, describing its features.
     *
     * @return A {@code EnumSet} with the flags of this message.
     */
    public EnumSet<Flag> getFlags() {
        if (data.getFlags() != null) {
            return Flag.of(data.getFlags());
        }
        return EnumSet.noneOf(Flag.class);
    }

    /**
     * Requests to retrieve the guild this message is associated to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this message is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getChannel().ofType(GuildChannel.class).flatMap(GuildChannel::getGuild);
    }

    /**
     * Gets the type of message.
     *
     * @return The type of message.
     */
    public Type getType() {
        return Type.of(data.getType());
    }

    /**
     * Requests to edit this message.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link MessageEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link Message}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> edit(final Consumer<? super MessageEditSpec> spec) {
        final MessageEditSpec mutatedSpec = new MessageEditSpec();
        spec.accept(mutatedSpec);

        return serviceMediator.getRestClient().getChannelService()
                .editMessage(getChannelId().asLong(), getId().asLong(), mutatedSpec.asRequest())
                .map(MessageBean::new)
                .map(bean -> new Message(serviceMediator, bean))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to delete this message.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the message has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this message while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the message has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return serviceMediator.getRestClient().getChannelService()
                .deleteMessage(getChannelId().asLong(), getId().asLong(), reason)
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to suppress all embeds in this message. If the message have the embeds suppressed then this action
     * can undo the suppressed embeds.
     *
     * @param suppress Determine if you need suppress or not the embeds.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the process has been
     * completed. If an error is received, it is emitted through the {@code Mono}.
     */
    @Experimental
    public Mono<Void> suppressEmbeds(final boolean suppress) {
        return serviceMediator.getRestClient().getChannelService()
                .suppressEmbeds(getChannelId().asLong(), getId().asLong(), new SuppressEmbedsRequest(suppress))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to add a reaction on this message.
     *
     * @param emoji The reaction to add on this message.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction was added on
     * this message. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> addReaction(final ReactionEmoji emoji) {
        return serviceMediator.getRestClient().getChannelService()
                .createReaction(getChannelId().asLong(), getId().asLong(), EntityUtil.getEmojiString(emoji))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to remove a reaction from a specified user on this message.
     *
     * @param emoji The reaction to remove on this message.
     * @param userId The user to remove the reaction on this message.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction from the
     * specified user was removed on this message. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeReaction(final ReactionEmoji emoji, final Snowflake userId) {
        return serviceMediator.getRestClient().getChannelService()
                .deleteReaction(getChannelId().asLong(), getId().asLong(), EntityUtil.getEmojiString(emoji),
                        userId.asLong())
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to remove a reaction from the current user on this message.
     *
     * @param emoji The reaction to remove on this message.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the reaction from the current
     * user was removed on this message. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeSelfReaction(final ReactionEmoji emoji) {
        return serviceMediator.getRestClient().getChannelService()
                .deleteOwnReaction(getChannelId().asLong(), getId().asLong(), EntityUtil.getEmojiString(emoji))
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to remove all the reactions on this message.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating all the reactions on this
     * message were removed. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeAllReactions() {
        return serviceMediator.getRestClient().getChannelService()
                .deleteAllReactions(getChannelId().asLong(), getId().asLong())
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to pin this message.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the messaged was pinned. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> pin() {
        return serviceMediator.getRestClient().getChannelService()
                .addPinnedMessage(getChannelId().asLong(), getId().asLong())
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Requests to unpin this message.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the message was unpinned. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> unpin() {
        return serviceMediator.getRestClient().getChannelService()
                .deletePinnedMessage(getChannelId().asLong(), getId().asLong())
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    /** Describes extra features of a message. */
    public enum Flag {

        /** This message has been published to subscribed channels (via Channel Following). */
        CROSSPOSTED(0),

        /** This message originated from a message in another channel (via Channel Following). */
        IS_CROSSPOST(1),

        /** Do not include any embeds when serializing this message. */
        SUPPRESS_EMBEDS(2);

        /** The underlying value as represented by Discord. */
        private final int value;

        /** The flag value as represented by Discord. */
        private final int flag;

        /**
         * Constructs a {@code Message.Flag}.
         */
        Flag(final int value) {
            this.value = value;
            this.flag = 1 << value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord.
         */
        public int getFlag() {
            return flag;
        }

        /**
         * Gets the flags of message. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<Flag> of(final int value) {
            final EnumSet<Flag> messageFlags = EnumSet.noneOf(Flag.class);
            for (Flag flag : Flag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    messageFlags.add(flag);
                }
            }
            return messageFlags;
        }
    }

    /** Represents the various types of messages. */
    public enum Type {

        /** A message created by a user. */
        DEFAULT(0),

        /** A message created when a recipient was added to a DM. */
        RECIPIENT_ADD(1),

        /** A message created when a recipient left a DM. */
        RECIPIENT_REMOVE(2),

        /** A message created when a call was started. */
        CALL(3),

        /** A message created when a channel's name changed. */
        CHANNEL_NAME_CHANGE(4),

        /** A message created when a channel's icon changed. */
        CHANNEL_ICON_CHANGE(5),

        /** A message created when a message was pinned. */
        CHANNEL_PINNED_MESSAGE(6),

        /** A message created when an user joins a guild. */
        GUILD_MEMBER_JOIN(7),

        /** A message created when an user boost a guild. */
        USER_PREMIUM_GUILD_SUBSCRIPTION(8),

        /** A message created when an user boost a guild and the guild reach the tier 1. */
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1(9),

        /** A message created when an user boost a guild and the guild reach the tier 2. */
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2(10),

        /** A message created when an user boost a guild and the guild reach the tier 3. */
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3(11),

        /**
         * A message created when a user follows a channel from another guild into specific channel (
         * <a href="https://support.discordapp.com/hc/en-us/articles/360028384531-Server-Following-FAQ">Server Following</a>).
         */
        CHANNEL_FOLLOW_ADD(12);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Message.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the type of message. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of message.
         */
        public static Type of(final int value) {
            switch (value) {
                case 0: return DEFAULT;
                case 1: return RECIPIENT_ADD;
                case 2: return RECIPIENT_REMOVE;
                case 3: return CALL;
                case 4: return CHANNEL_NAME_CHANGE;
                case 5: return CHANNEL_ICON_CHANGE;
                case 6: return CHANNEL_PINNED_MESSAGE;
                case 7: return GUILD_MEMBER_JOIN;
                case 8: return USER_PREMIUM_GUILD_SUBSCRIPTION;
                case 9: return USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1;
                case 10: return USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2;
                case 11: return USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3;
                case 12: return CHANNEL_FOLLOW_ADD;
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "data=" + data +
                '}';
    }
}
