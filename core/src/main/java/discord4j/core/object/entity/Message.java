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

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.Reaction;
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.bean.MessageBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Discord message.
 * <p>
 * <a href="https://discordapp.com/developers/docs/resources/channel#message-object">Message Object</a>
 */
public final class Message implements Entity {

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
        return getClient().getMessageChannelById(getChannelId());
    }

    /**
     * Gets the ID of the author of this message, if present.
     *
     * @return The ID of the author of this message, if present.
     */
    public Optional<Snowflake> getAuthorId() {
        // author is not a real user if webhook is present (message was sent by webhook)
        return Optional.of(data.getAuthor())
                .filter(ignored -> !getWebhookId().isPresent())
                .map(Snowflake::of);
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
     * Requests to retrieve the author of this message, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User author} of this message, if
     * present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getAuthor() {
        return Mono.justOrEmpty(getAuthorId()).flatMap(getClient()::getUserById);
    }

    /**
     * Gets the contents of the message, if present.
     *
     * @return The contents of the message, if present.
     */
    public Optional<String> getContent() {
        return Optional.ofNullable(data.getContent());
    }

    /**
     * Gets when this message was sent.
     *
     * @return When this message was sent.
     */
    public Instant getTimestamp() {
        return Instant.parse(data.getTimestamp());
    }

    /**
     * Gets when this message was edited, if present.
     *
     * @return When this message was edited, if present.
     */
    public Optional<Instant> getEditedTimestamp() {
        return Optional.ofNullable(data.getEditedTimestamp()).map(Instant::parse);
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
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    // TODO: getEmbeds()

    /**
     * Gets the IDs of any attached files.
     *
     * @return The IDs of any attached files.
     */
    public Set<Snowflake> getAttachmentIds() {
        return Arrays.stream(data.getAttachments())
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Requests to retrieve any attached files.
     *
     * @return A {@link Flux} that continually emits any {@link Attachment attached} files. If an error is received, it
     * is emitted through the {@code Flux}.
     */
    public Flux<Attachment> getAttachments() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the reactions to this message.
     *
     * @return The reactions to this message.
     */
    public Set<Reaction> getReactions() {
        throw new UnsupportedOperationException("Not yet implemented...");
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
     * Gets the type of message.
     *
     * @return The type of message.
     */
    public Type getType() {
        return Type.of(data.getType());
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
        GUILD_MEMBER_JOIN(7);

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
         * Gets the type of message. It is guaranteed that invoking {@link #getValue()} from the returned enum will
         * equal ({@code ==}) the supplied {@code value}.
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
                default: throw new UnsupportedOperationException("Unknown Value: " + value);
            }
        }
    }
}
