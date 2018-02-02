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

import discord4j.common.json.response.MessageResponse;
import discord4j.core.Client;
import discord4j.core.object.Reaction;
import discord4j.core.object.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Discord message.
 *
 * <a href="https://discordapp.com/developers/docs/resources/channel#message-object">Message Object</a>
 */
public final class Message implements Entity {

	/** The Client associated to this object. */
	private final Client client;

	/** The raw data as represented by Discord. */
	private final MessageResponse message;

	/**
	 * Constructs a {@code Message} with an associated client and Discord data.
	 *
	 * @param client The Client associated to this object, must be non-null.
	 * @param message The raw data as represented by Discord, must be non-null.
	 */
	public Message(final Client client, final MessageResponse message) {
		this.client = Objects.requireNonNull(client);
		this.message = Objects.requireNonNull(message);
	}

	@Override
	public Client getClient() {
		return client;
	}

	@Override
	public Snowflake getId() {
		return Snowflake.of(message.getId());
	}

	/**
	 * Gets the ID of the channel the message was sent in.
	 *
	 * @return The ID of the channel the message was sent in.
	 */
	public Snowflake getChannelId() {
		return Snowflake.of(message.getChannelId());
	}

	/**
	 * Requests to retrieve the channel the message was sent in.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel channel} the message
	 * was sent in. If an error is received, it is emitted through the {@code Mono}.
	 */
	public Mono<MessageChannel> getChannel() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the ID of the author of this message, if present.
	 *
	 * @return The ID of the author of this message, if present.
	 */
	public Optional<Snowflake> getAuthorId(){
		return Optional.of(message.getAuthor())
				// If the webhook is present then the user is not valid
				.filter(ignored -> !message.getWebhookId().isPresent())
				.map(author -> Snowflake.of(author.getId()));
	}

	/**
	 * Requests to retrieve the author of this message, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link User author} of this message, if
	 * present. If an error is received, it is emitted through the {@code Mono}.
	 */
	public Mono<User> getAuthor() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the contents of the message, if present.
	 *
	 * @return The contents of the message, if present.
	 */
	public Optional<String> getContent() {
		return Optional.ofNullable(message.getContent());
	}

	/**
	 * Gets when this message was sent.
	 *
	 * @return When this message was sent.
	 */
	public Instant getTimestamp() {
		return Instant.parse(message.getTimestamp());
	}

	/**
	 * Gets when this message was edited, if present.
	 *
	 * @return When this message was edited, if present.
	 */
	public Optional<Instant> getEditedTimestamp() {
		return Optional.ofNullable(message.getEditedTimestamp()).map(Instant::parse);
	}

	/**
	 * Gets whether this was a TTS (Text-To-Speech) message.
	 *
	 * @return {@code true} if this message was a TTS (Text-To-Speech) message, {@code false} otherwise.
	 */
	public boolean isTts() {
		return message.isTts();
	}

	/**
	 * Gets whether this message mentions everyone.
	 *
	 * @return {@code true} if this message mentions everyone, {@code false} otherwise.
	 */
	public boolean mentionsEveryone() {
		return message.isMentionEveryone();
	}

	/**
	 * Gets the IDs of the users specifically mentioned in this message.
	 *
	 * @return The IDs of the users specifically mentioned in this message.
	 */
	public Set<Snowflake> getUserMentionIds() {
		return Arrays.stream(message.getMentions())
				.map(user -> Snowflake.of(user.getId()))
				.collect(Collectors.toSet());
	}

	/**
	 * Requests to retrieve the users specifically mentioned in this message.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link User user} specifically mentioned in
	 * this message. If an error is received, it is emitted through the {@link Mono}.
	 */
	public Flux<User> getUserMentions() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the IDs of the roles specifically mentioned in this message.
	 *
	 * @return The IDs of the roles specifically mentioned in this message.
	 */
	public Set<Snowflake> getRoleMentionIds() {
		return Arrays.stream(message.getMentionRoles())
				.mapToObj(Snowflake::of)
				.collect(Collectors.toSet());
	}

	/**
	 * Requests to retrieve the roles specifically mentioned in this message.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Role roles} specifically mentioned in
	 * this message. If an error is received, it is emitted through the {@code Mono}.
	 */
	public Flux<Role> getRoleMentions() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets any attached files.
	 *
	 * @return Any attached files.
	 */
	public Set<Attachment> getAttachments() {
		return Arrays.stream(message.getAttachments())
				.map(attachment -> new Attachment(client, attachment))
				.collect(Collectors.toSet());
	}

	// TODO: getEmbeds()

	/**
	 * Gets the reactions to this message.
	 *
	 * @return The reactions to this message.
	 */
	public Set<Reaction> getReactions() {
		return Optional.ofNullable(message.getReactions())
				.map(Arrays::stream)
				.map(reactions -> reactions.map(reaction -> new Reaction(client, reaction, message.getId())))
				.map(reactions -> reactions.collect(Collectors.toSet()))
				.orElse(Collections.emptySet());
	}

	/**
	 * Gets whether this message is pinned.
	 *
	 * @return {@code true} if this message is pinned, {@code false} otherwise.
	 */
	public boolean isPinned() {
		return message.isPinned();
	}

	/**
	 * Gets the ID the webhook that generated this message, if present.
	 *
	 * @return The ID of the webhook that generated this message, if present.
	 */
	public Optional<Snowflake> getWebhookId() {
		final OptionalLong id = message.getWebhookId();
		return id.isPresent() ? Optional.of(Snowflake.of(id.getAsLong())) : Optional.empty();
	}

	/**
	 * Requests to retrieve the webhook that generated this message, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook webhook} that generated this
	 * message, if present. If an error is received, it is emitted through the {@code Mono}.
	 */
	public Mono<Webhook> getWebhook() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	/**
	 * Gets the type of message.
	 *
	 * @return The type of message.
	 */
	public Type getType() {
		return Arrays.stream(Type.values())
				.filter(value -> value.value == message.getType())
				.findFirst() // If this throws Discord added something
				.orElseThrow(UnsupportedOperationException::new);
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
	}
}
