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

import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/** Represents a reaction to a {@link Message}. */
public interface Reaction {

	/**
	 * Gets the number times this emoji has been used to react.
	 *
	 * @return The number of times this emoji has been used to react.
	 */
	int getCount();

	/**
	 * Gets whether the current user reacted using this emoji.
	 *
	 * @return {@code true} if the current user reacted using this emoji, {@code false} otherwise.
	 */
	boolean hasReacted();

	/**
	 * Gets the ID of the emoji for this reaction, if present.
	 *
	 * @return The ID of the emoji for this reaction, if present.
	 */
	Optional<Snowflake> getEmojiId();

	/**
	 * Requests to retrieve the emoji for this reaction, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji emoji} for this reaction,
	 * if present. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<GuildEmoji> getGuildEmoji();

	/**
	 * Gets the name of the emoji for this reaction.
	 *
	 * @return The name of the emoji for this reaction.
	 */
	String getEmojiName();

	/**
	 * Gets the ID of the message this reaction is associated to.
	 *
	 * @return The ID of the message this reaction is associated to.
	 */
	Snowflake getMessageId();

	/**
	 * Requests to retrieve the message this reaction is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Message message} this reaction is
	 * associated to. If an error is received it is emitted through the {@code Mono}.
	 */
	Mono<Message> getMessage();

	/**
	 * Requests to retrieve the users that reacted with this emoji.
	 *
	 * @return A {@link Flux} that continually emits the {@link User users} that reacted with this emoji. If an error is
	 * received, it is emitted through the {@code Flux}.
	 */
	Flux<User> getReactors();
}
