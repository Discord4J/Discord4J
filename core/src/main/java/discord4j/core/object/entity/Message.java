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

import discord4j.core.object.Reaction;
import discord4j.core.object.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/** A Discord message. */
public interface Message extends Entity {

	/**
	 * Gets the ID of the channel the message was sent in.
	 *
	 * @return The ID of the channel the message was sent in.
	 */
	Snowflake getChannelId();

	/**
	 * Requests to retrieve the channel the message was sent in.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel channel} the message
	 * was sent in. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<MessageChannel> getChannel();

	/**
	 * Gets the ID of the author of this message, if present.
	 *
	 * @return The ID of the author of this message, if present.
	 */
	Optional<Snowflake> getAuthorId();

	/**
	 * Requests to retrieve the author of this message, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link User author} of this message, if
	 * present. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<User> getAuthor();

	/**
	 * Gets the contents of the message.
	 *
	 * @return The contents of the message.
	 */
	String getContent();

	/**
	 * Gets when this message was sent.
	 *
	 * @return When this message was sent.
	 */
	Instant getTimestamp();

	/**
	 * Gets when this message was edited, if present.
	 *
	 * @return When this message was edited, if present.
	 */
	Optional<Instant> getEditedTimestamp();

	/**
	 * Gets whether this was a TTS (Text-To-Speech) message.
	 *
	 * @return {@code true} if this message was a TTS (Text-To-Speech) message, {@code false} otherwise.
	 */
	boolean isTts();

	/**
	 * Gets whether this message mentions everyone.
	 *
	 * @return {@code true} if this message mentions everyone, {@code false} otherwise.
	 */
	boolean mentionsEveryone();

	/**
	 * Gets the IDs of the users specifically mentioned in this message.
	 *
	 * @return The IDs of the users specifically mentioned in this message.
	 */
	Set<Snowflake> getUserMentionIds();

	/**
	 * Requests to retrieve the users specifically mentioned in this message.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link User user} specifically mentioned in
	 * this message. If an error is received, it is emitted through the {@link Mono}.
	 */
	Flux<User> getUserMentions();

	/**
	 * Gets the IDs of the roles specifically mentioned in this message.
	 *
	 * @return The IDs of the roles specifically mentioned in this message.
	 */
	Set<Snowflake> getRoleMentionIds();

	/**
	 * Requests to retrieve the roles specifically mentioned in this message.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Role roles} specifically mentioned in
	 * this message. If an error is received, it is emitted through the {@code Mono}.
	 */
	Flux<Role> getRoleMentions();

	/**
	 * Gets any attached files.
	 *
	 * @return Any attached files.
	 */
	Set<Attachment> getAttachments();

	// TODO: getEmbeds()

	/**
	 * Gets the reactions to this message.
	 *
	 * @return The reactions to this message.
	 */
	Set<Reaction> getReactions();

	/**
	 * Gets whether this message is pinned.
	 *
	 * @return {@code true} if this message is pinned, {@code false} otherwise.
	 */
	boolean isPinned();

	/**
	 * Gets the ID the webhook that generated this message, if present.
	 *
	 * @return The ID of the webhook that generated this message, if present.
	 */
	Optional<String> getWebhookId();

	/**
	 * Requests to retrieve the webhook that generated this message, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook webhook} that generated this
	 * message, if present. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Webhook> getWebhook();

	/**
	 * Gets the type of message.
	 *
	 * @return The type of message.
	 */
	Type getType();

	/** Represents the various types of messages. */
	enum Type {

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
	}
}
